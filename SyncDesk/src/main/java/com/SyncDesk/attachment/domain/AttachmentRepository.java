package com.syncdesk.attachment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository {

    Optional<TicketAttachment> findById(UUID id);

    List<TicketAttachment> findByTicketId(UUID ticketId);

    void deleteById(UUID id);
}
