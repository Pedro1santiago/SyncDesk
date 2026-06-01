package com.syncdesk.attachment.domain;

import com.syncdesk.ticket.domain.entity.Ticket;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_attachments")
public class TicketAttachment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public TicketAttachment(Ticket ticket, String fileName, String fileUrl) {
        this.id = UUID.randomUUID();
        this.ticket = ticket;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }
}
