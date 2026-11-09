# Music Server 虚拟机公网直连部署流程

本文以 `CentOS-7-x86_64-Everything-2009.iso` 安装的 CentOS 7.9 虚拟机为例，把 `music-server` 后端部署到 VMware，并通过公网 IP 和域名直连验证。当前范围只有 Java Web 后端，不部署前端、不配置雷池。

> **维护状态：** CentOS Linux 7 已于 2024 年 6 月 30 日结束生命周期，不再获得常规安全更新。[CentOS 官方说明](https://www.centos.org/centos-linux/) 本文适合实验学习；如果要长期开放公网服务，建议改用仍受支持的系统。

## 1. 项目情况和部署边界

- 项目使用 Spring Boot 2.6.2、Maven，目标 Java 8，默认 HTTP 端口为 `8888`。
- 默认 `dev` 配置连接本机 MySQL `3306`、Redis `6379`、MinIO `9000`。三者都是应用依赖，不能只启动 Java。
- 仓库根目录的 `tp_music.sql` 用于初始化数据库。歌曲和图片还依赖 MinIO 对象及本地媒体目录。
- 仓库现有 `docker-compose.yml` 是 Windows 开发配置：MySQL 映射为 `3307:3306`，MinIO 使用 `D:/minio/data`。不能原样复制到 CentOS。
- `application-prod.properties` 中有外部数据库地址和硬编码凭据，不要直接启用。本文通过 systemd 环境文件覆盖 `dev` 配置并连接 VM 本机依赖。
- 本机此前 `/songList`、`/singer` 接口返回过 200，而 `/banner/getAllBanner` 返回 500。部署后仍需检查该接口对应的应用日志和数据。

公网访问路径：

```text
访问者 -> Cloudflare DNS A 记录 -> 路由器公网 IPv4:8888
       -> 路由器端口转发 -> CentOS VM 局域网 IPv4:8888
       -> firewalld -> Spring Boot
```

VMware NAT 通常只允许虚拟机主动访问外网。这里使用 VMware **桥接网络**，使 VM 从路由器获取同网段地址；路由器再将公网端口转发到 VM。桥接不等于公网 IP。若 WAN 地址处于私网/运营商级 NAT，或者运营商屏蔽入站连接，端口转发无法从公网到达；Cloudflare DNS 也不能穿透 CGNAT。

## 2. 先确认是否有公网入口

登录路由器管理页记录 WAN IPv4，再从主机查询当前公网 IPv4。不要在聊天、截图或公开文档中暴露完整地址。

- WAN 地址与查询到的地址相同，通常具备公网 IPv4 条件，但仍需外网端口测试。
- WAN 地址属于以下范围，通常是私网或运营商级 NAT：`10.0.0.0/8`、`172.16.0.0/12`、`192.168.0.0/16`、`100.64.0.0/10`。
- 地址不同或处于这些范围时，向运营商确认能否申请公网 IPv4 和入站连接。不要期待 DNS 记录绕过 NAT。
- 若公网 IPv4 动态变化，之后需要配置 DDNS 更新 Cloudflare A 记录。

## 3. VMware 桥接和 CentOS 网络

VM 建议至少 2 vCPU、4 GB 内存、30 GB 磁盘。数据库、Redis、MinIO 和音乐媒体都放在此 VM 时，应按实际文件量增加磁盘。

1. 在 VMware 虚拟机设置中将 Network Adapter 设为 **Bridged**。若主机通过 Wi-Fi 上网，在 Virtual Network Editor 中确认 VMnet0 桥接到当前 WLAN。
2. 启动 CentOS，查看网络接口和路由：

   ```bash
   ip addr
   ip route
   nmcli device status
   ```

   记录活动网卡名（如 `ens33`）及其局域网 IPv4，例如 `192.168.13.50`。确认有指向路由器的默认路由。

3. 从虚拟机检查外网连通：

   ```bash
   ping -c 3 1.1.1.1
   ```

   再从 Windows 主机运行 `ping <VM_LAN_IP>`。如不通，先检查桥接网卡、DHCP、Wi-Fi 客户端隔离及 CentOS 网络状态。

4. 在路由器为 VM 设置 DHCP 地址保留，确保端口转发目标地址稳定。CentOS 7 使用 NetworkManager，可用 `nmtui` 配置网络；初学时优先使用路由器 DHCP 保留，避免手动网关/DNS 配错。

## 4. 更新 CentOS 7 并安装基础软件

CentOS 7 已停止维护，默认 mirrorlist 可能不可用。若 `yum` 报仓库地址失效，需将 CentOS 7 官方仓库地址调整到 Vault 归档仓库；这只能解决软件包下载，不会恢复安全更新。以下操作只用于实验 VM。

```bash
sudo yum clean all
sudo yum makecache
sudo yum update -y
sudo yum install -y java-1.8.0-openjdk-devel git curl wget tar firewalld
sudo systemctl enable firewalld
sudo systemctl start firewalld
java -version
```

检查应显示 Java 1.8。Docker Engine 的当前发行版可能已不再支持 CentOS 7；实验中可安装与 CentOS 7 兼容的 Docker CE 旧版本，但这属于停止维护的软件，不应用于长期公网生产。若安装源不可用，可在 VM 外部受控环境准备兼容 Docker 包，或将 MySQL/Redis/MinIO 安装在独立且受支持的主机上。

安装 Maven：

```bash
sudo yum install -y maven
mvn -version
```

如果 CentOS 仓库没有 Maven，可用项目自带的 `mvnw` 构建，无需系统 Maven。`mvnw` 首次运行需要联网下载 Maven 和项目依赖。

## 5. 准备 MySQL、Redis、MinIO

本节假设 VM 已有可用 Docker Engine 与 Compose V2。若只能安装 CentOS 7 常见的 Docker Compose V1，需将后续 `docker compose` 改为 `docker-compose`，并使用 V1 支持的 `docker-compose.yml` 文件名。

不要复制项目的 Windows Compose 文件。为 Linux VM 新建目录：

```bash
sudo mkdir -p /opt/music/data/{mysql,redis,minio} /opt/music/app
sudo chown -R "$USER":"$USER" /opt/music
cd /opt/music
vi compose.yaml
```

写入以下示例，并将三个示例密码替换成不同的强密码。数据服务端口只绑定 `127.0.0.1`，不向局域网或公网开放：

```yaml
services:
  mysql:
    image: mysql:8.0
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: "替换为MySQL强密码"
      MYSQL_DATABASE: tp_music
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    ports:
      - "127.0.0.1:3306:3306"
    volumes:
      - ./data/mysql:/var/lib/mysql

  redis:
    image: redis:7-alpine
    restart: unless-stopped
    command: ["redis-server", "--appendonly", "yes", "--requirepass", "替换为Redis强密码"]
    ports:
      - "127.0.0.1:6379:6379"
    volumes:
      - ./data/redis:/data

  minio:
    image: minio/minio:RELEASE.2024-01-16T16-07-38Z
    restart: unless-stopped
    environment:
      MINIO_ROOT_USER: musicadmin
      MINIO_ROOT_PASSWORD: "替换为MinIO强密码"
    command: server /data --console-address ":9001"
    ports:
      - "127.0.0.1:9000:9000"
      - "127.0.0.1:9001:9001"
    volumes:
      - ./data/minio:/data
```

使用已安装的 Compose 命令启动：

```bash
cd /opt/music
sudo docker compose up -d
sudo docker compose ps
sudo docker compose logs --tail=100 mysql redis minio
```

若用 Compose V1，把命令写为 `sudo docker-compose up -d` 等。若 MinIO 固定版本标签已不可拉取，应选择官方仍提供的固定版本，避免 `latest` 导致重启时版本漂移。Compose 密码需与 Java 环境文件一致。

### 导入数据库

将 `D:\graduate_music\tp_music.sql` 复制到 VM 的 `/opt/music/tp_music.sql`。检查 SQL 内容是否包含 `CREATE DATABASE` 或 `USE`，并确认没有对已有数据执行删表/覆盖操作。交互读取密码后导入，避免将密码写入 shell 历史：

```bash
cd /opt/music
read -rsp 'MySQL root password: ' MYSQL_PWD; echo
export MYSQL_PWD
sudo --preserve-env=MYSQL_PWD docker compose exec -T -e MYSQL_PWD mysql mysql -uroot tp_music < /opt/music/tp_music.sql
unset MYSQL_PWD
```

若使用 Compose V1，将命令中的 `docker compose` 换为 `docker-compose`。检查表：

```bash
read -rsp 'MySQL root password: ' MYSQL_PWD; echo
export MYSQL_PWD
sudo --preserve-env=MYSQL_PWD docker compose exec -T -e MYSQL_PWD mysql mysql -uroot -e 'USE tp_music; SHOW TABLES;'
unset MYSQL_PWD
```

## 6. 构建 Spring Boot 并配置运行服务

### 6.1 构建 jar

将 `music-server` 源码复制或克隆至 VM 的 `/opt/music/source`，然后执行：

```bash
cd /opt/music/source
chmod +x mvnw
./mvnw clean package -DskipTests
```

生成文件应为 `target/yin-0.0.1-SNAPSHOT.jar`。如果 VM 无法访问 Maven 仓库，可在可信构建机执行同一命令，再把 jar 复制到 VM。

### 6.2 设置应用连接参数

不要激活仓库当前的 `prod` profile：该配置包含外部数据库地址和硬编码凭据。创建权限为 600 的 systemd 环境文件：

```bash
sudo touch /etc/music-server.env
sudo chmod 600 /etc/music-server.env
sudo vi /etc/music-server.env
```

填入配置并替换密码：

```ini
SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/tp_music?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=替换为MySQL强密码
SPRING_REDIS_HOST=127.0.0.1
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=替换为Redis强密码
MINIO_ENDPOINT=http://127.0.0.1:9000
MINIO_ACCESS_KEY=musicadmin
MINIO_SECRET_KEY=替换为MinIO强密码
MINIO_BUCKET_NAME=user01
```

项目的 MinIO 客户端使用 `user01` bucket。启动 Java 前应在 `http://127.0.0.1:9001` 管理页面创建该 bucket，并确保应用凭据有读写权限。MinIO 控制台端口仅绑定 VM 回环地址。

### 6.3 准备静态媒体目录

代码以 JVM 工作目录作为本地媒体根目录，使用 `/img/...` 和 `/song/...` URL。部署目录需准备：

```text
/opt/music/app/img/avatorImages/
/opt/music/app/img/singerPic/
/opt/music/app/img/songPic/
/opt/music/app/img/songListPic/
/opt/music/app/img/swiper/
/opt/music/app/song/
```

创建目录并准备专用运行账号：

```bash
sudo useradd --system --home /opt/music/app --shell /sbin/nologin music
sudo mkdir -p /opt/music/app/img/{avatorImages,singerPic,songPic,songListPic,swiper} /opt/music/app/song
sudo cp /opt/music/source/target/yin-0.0.1-SNAPSHOT.jar /opt/music/app/app.jar
sudo chown -R music:music /opt/music/app
```

把数据库中引用的媒体文件复制到对应目录，并确保 MinIO bucket 对象已迁移或重新上传。导入 SQL 不会自动复制媒体对象。

### 6.4 配置 systemd

```bash
sudo vi /etc/systemd/system/music-server.service
```

写入：

```ini
[Unit]
Description=Music Java Web
After=network-online.target docker.service
Wants=network-online.target

[Service]
Type=simple
User=music
WorkingDirectory=/opt/music/app
EnvironmentFile=/etc/music-server.env
ExecStart=/usr/bin/java -Djava.security.egd=file:/dev/./urandom -jar /opt/music/app/app.jar --server.address=0.0.0.0 --server.port=8888
Restart=on-failure
RestartSec=5
SuccessExitStatus=143
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

加载并启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable music-server
sudo systemctl start music-server
sudo systemctl status music-server --no-pager
sudo journalctl -u music-server -n 100 --no-pager
```

`WorkingDirectory` 必须是 `/opt/music/app`，因为代码里的静态资源目录相对于 `user.dir` 解析。

## 7. 逐层验收

### 7.1 VM 本机

```bash
curl -i http://127.0.0.1:8888/songList
curl -i http://127.0.0.1:8888/singer
sudo ss -lntp | grep 8888
```

API 应返回 HTTP 200 和 JSON，监听地址应为 `0.0.0.0:8888` 或 `*:8888`。根路径 `/` 返回 404 可以是正常现象，因为后端没有首页路由。

### 7.2 局域网

从 Windows 主机或同一 Wi-Fi 下的手机访问：

```text
http://<VM_LAN_IP>:8888/songList
```

CentOS 7 默认使用 firewalld。先检查区域与规则：

```bash
sudo systemctl status firewalld
sudo firewall-cmd --get-active-zones
sudo firewall-cmd --list-all
```

确定活动区域（通常为 `public`）后，仅开放 Java API 端口：

```bash
sudo firewall-cmd --zone=public --add-port=8888/tcp --permanent
sudo firewall-cmd --reload
sudo firewall-cmd --zone=public --list-ports
```

不要开放 MySQL `3306`、Redis `6379`、MinIO `9000/9001`。如果 VM 本机可访问而 LAN 设备不通，检查 `server.address`、firewalld、Wi-Fi 客户端隔离及 VM 是否仍处于 NAT 网络。

## 8. 路由器端口转发和公网 IP 验证

仅在局域网测试通过后进行：

1. 在路由器添加 TCP 端口转发：外部端口 `8888` → VM 固定局域网 IP → 内部端口 `8888`。
2. 确认 CentOS firewalld 已允许 `8888/tcp`。不要关闭整个防火墙；如需远程管理，还要保留当前 SSH 访问规则。
3. 用手机蜂窝网络或其他外部网络访问 `http://<公网IPv4>:8888/songList`。不要在同一 Wi-Fi 下测试公网地址，因为路由器可能不支持 NAT loopback。
4. 外网返回 JSON 才证明端口转发可达。如果失败，重新核对公网 IPv4、双重 NAT、路由规则、VM IP 和运营商入站限制。

这是临时教学验证：裸 HTTP 会明文传输，直接公开 8888 也会让 API 暴露给互联网扫描。不要用它长期承载真实账号、密码或私人数据。长期运行至少要使用 HTTPS、维护系统与依赖、限制入口并定期备份。本文暂不配置雷池。

## 9. Cloudflare DNS 域名验证

在 Cloudflare DNS 新增记录：

- 类型：`A`
- 名称：例如 `music-api`，最终域名为 `music-api.example.com`
- IPv4 地址：路由器当前公网 IPv4
- 代理状态：**DNS only（灰云）**
- TTL：Auto

检查解析：

```powershell
nslookup music-api.example.com
```

返回地址应与路由器 WAN 公网 IPv4 相同。然后从外网访问：

```text
http://music-api.example.com:8888/songList
```

本阶段必须使用灰云。Cloudflare 橙云 HTTP 代理不代理任意 `8888` 端口；DNS 解析本身也不提供 NAT 穿透。公网 IP 动态变化时配置 DDNS，API Token 应限权并妥善保存，不要提交到 Git。

## 10. 常见问题排查

| 现象 | 检查内容 |
| --- | --- |
| yum 无法下载软件 | CentOS 7 仓库已归档，检查 Vault 仓库配置；不要把停更系统当作长期公网系统 |
| VM 无法联网 | VMware 桥接网卡、`ip route` 默认路由、Wi-Fi 桥接限制和 DNS |
| VM 本机 API 连接被拒绝 | `systemctl status`、`journalctl`、Java 监听端口、MySQL/Redis/MinIO 状态 |
| VM 本机通，局域网不通 | `server.address`、firewalld 活动区域和 8888 规则、路由器客户端隔离、VM 网络模式 |
| 局域网通，公网不通 | 公网 IPv4、WAN 与外部查询地址、端口转发、双重 NAT、运营商封锁、firewalld |
| IP 通，域名不通 | Cloudflare A 记录、DNS 缓存、是否误开橙云 |
| `/songList` 返回 500 | MySQL 是否启动、`tp_music` 是否导入、账号密码和应用日志 |
| `/banner/getAllBanner` 返回 500 | 查看 `journalctl -u music-server` 中异常堆栈及 Banner、Redis、数据库数据 |
| API 正常但图片/歌曲 404 | MinIO bucket/object 是否存在，本地 `/opt/music/app/img`、`song` 文件和数据库路径是否对应 |
| 数据服务能从外网访问 | Compose 端口必须绑定 `127.0.0.1`；关闭多余端口并移除错误的路由器转发 |

按“VM 本机 → 局域网 → 公网 IP → 域名”顺序测试，每次先修复当前失败层。

## 11. 日常操作和回滚

```bash
sudo systemctl stop music-server
sudo systemctl start music-server
sudo systemctl restart music-server
sudo journalctl -u music-server -f
cd /opt/music && sudo docker compose ps
cd /opt/music && sudo docker compose logs -f mysql redis minio
```

如果使用 Compose V1，将 `docker compose` 替换为 `docker-compose`。升级 jar 前备份数据库、`/opt/music/data`、`/opt/music/app/img` 和 `/opt/music/app/song`。升级失败时恢复旧 jar 并重启服务。

## 12. 完成检查清单

- [ ] VMware 使用桥接网络，CentOS VM 有固定局域网 IPv4 和默认路由。
- [ ] MySQL、Redis、MinIO 已启动，端口仅绑定 VM 回环地址。
- [ ] `tp_music.sql` 已导入，MinIO bucket 和媒体文件已准备。
- [ ] Spring Boot 以 Java 8 运行，systemd 可启动服务，日志无依赖连接错误。
- [ ] VM 本机和局域网设备访问 `/songList`、`/singer` 成功。
- [ ] 路由器 WAN 地址是公网 IPv4，外部网络通过公网 IP:8888 访问成功。
- [ ] Cloudflare A 记录为 DNS only，外网通过域名:8888 返回 API JSON。
- [ ] MySQL、Redis、MinIO 管理端口没有开放到 WAN。

公网前置条件不满足时，先记录失败层并停止调试 DNS；公网直连必须有可路由的公网地址和有效入站连接。
