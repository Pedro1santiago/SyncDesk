package com.syncdesk.attachment.application;

import com.syncdesk.attachment.domain.AttachmentRepository;
import com.syncdesk.shared.exception.NotFoundException;
import com.syncdesk.ticket.application.dto.TicketAttachmentResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public TicketAttachmentResponse findById(UUID id) {
        log.debug("Finding attachment by id={}", id);
        return attachmentRepository.findById(id)
                .map(TicketAttachmentResponse::from)
                .orElseThrow(() -> NotFoundException.of("Attachment", id));
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting attachment id={}", id);
        attachmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Attachment", id));
        attachmentRepository.deleteById(id);
        log.info("Attachment deleted: id={}", id);
    }
}
