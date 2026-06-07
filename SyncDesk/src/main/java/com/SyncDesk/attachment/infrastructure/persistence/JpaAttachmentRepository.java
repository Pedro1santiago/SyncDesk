package com.syncdesk.attachment.infrastructure.persistence;

import com.syncdesk.attachment.domain.AttachmentRepository;
import com.syncdesk.attachment.domain.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAttachmentRepository extends JpaRepository<TicketAttachment, UUID>, AttachmentRepository {

    List<TicketAttachment> findByTicketId(UUID ticketId);
}
