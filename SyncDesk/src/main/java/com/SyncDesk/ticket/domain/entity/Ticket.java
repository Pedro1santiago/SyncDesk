package com.syncdesk.ticket.domain.entity;

import com.syncdesk.attachment.domain.TicketAttachment;
import com.syncdesk.department.domain.Department;
import com.syncdesk.ticket.domain.enums.TicketPriority;
import com.syncdesk.ticket.domain.enums.TicketStatus;
import com.syncdesk.ticket.domain.exception.TicketClosedException;
import com.syncdesk.ticket.domain.valueobject.Title;
import com.syncdesk.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Embedded
    private Title title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketMessage> messages = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TicketDepartment> ticketDepartments = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Ticket(User user, Title title, String description, TicketPriority priority) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
    }

    public void assignTo(User agent) {
        rejectIfClosed();
        this.assignedUser = agent;
        if (TicketStatus.OPEN.equals(this.status)) {
            this.status = TicketStatus.IN_PROGRESS;
        }
    }

    public void changeStatus(TicketStatus newStatus) {
        rejectIfClosed();
        this.status = newStatus;
    }

    public void changePriority(TicketPriority newPriority) {
        rejectIfClosed();
        this.priority = newPriority;
    }

    public TicketMessage addMessage(User sender, String message) {
        rejectIfClosed();
        TicketMessage msg = new TicketMessage(this, sender, message);
        this.messages.add(msg);
        return msg;
    }

    public TicketAttachment addAttachment(String fileName, String fileUrl) {
        rejectIfClosed();
        TicketAttachment attachment = new TicketAttachment(this, fileName, fileUrl);
        this.attachments.add(attachment);
        return attachment;
    }

    public void assignDepartment(Department department) {
        rejectIfClosed();
        boolean alreadyAssigned = this.ticketDepartments.stream()
                .anyMatch(td -> td.getDepartment().getId().equals(department.getId()));
        if (alreadyAssigned) {
            return;
        }
        this.ticketDepartments.add(new TicketDepartment(this, department));
    }

    public void close() {
        rejectIfClosed();
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public boolean belongsTo(UUID userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isAssignedTo(UUID userId) {
        return this.assignedUser != null && this.assignedUser.getId().equals(userId);
    }

    public boolean isInDepartment(com.syncdesk.department.domain.Department department) {
        if (department == null) return false;
        return ticketDepartments.stream()
                .anyMatch(td -> td.getDepartment().getId().equals(department.getId()));
    }

    private void rejectIfClosed() {
        if (TicketStatus.CLOSED.equals(this.status)) {
            throw new TicketClosedException(this.id.toString());
        }
    }
}
