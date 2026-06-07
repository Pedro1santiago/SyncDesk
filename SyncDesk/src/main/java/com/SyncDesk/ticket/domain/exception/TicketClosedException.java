package com.syncdesk.ticket.domain.exception;

public class TicketClosedException extends RuntimeException {

    public TicketClosedException(String ticketId) {
        super("Ticket " + ticketId + " is closed and cannot be modified");
    }
}
