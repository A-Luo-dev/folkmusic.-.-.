package com.example.yin.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class PostCreateRequest {

    @NotBlank(message = "帖子内容不能为空")
    private String content;

    // 可选：标题
    private String title;

    // 是否原创：true/false -> 数据库存 1/0
    private Boolean isOriginal;

    // 可选：标签，用逗号分隔，或你后续改成数组/JSON
    private List<String> tags;

    // 附件列表（可空）
    private List<AttachmentDTO> attachments;

    @Data
    public static class AttachmentDTO {
        // 附件ID（用于更新已上传的附件）
        private Long attachmentId;
        
        // image / audio / video / link
        private String type;

        // 文件/链接地址（你现有的文件服务返回的 URL 或相对路径）
        private String url;

        // 可选：文件大小、时长、排序
        private Long fileSize;
        private Integer duration;
        private Integer sortOrder;
    }
}
