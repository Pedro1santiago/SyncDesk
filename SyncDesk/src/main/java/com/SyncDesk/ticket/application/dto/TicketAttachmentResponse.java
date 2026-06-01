package com.syncdesk.ticket.application.dto;

import com.syncdesk.attachment.domain.TicketAttachment;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketAttachmentResponse(
        UUID id,
        String fileName,
        String fileUrl,
        LocalDateTime uploadedAt
) {
    public static TicketAttachmentResponse from(TicketAttachment attachment) {
        return new TicketAttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFileUrl(),
                attachment.getUploadedAt()
        );
    }
}
