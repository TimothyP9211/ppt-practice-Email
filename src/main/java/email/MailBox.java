package email;

import java.util.*;

/**
 * A datatype that represents a mailbox or collection of email.
 */
public class MailBox {
    // TODO Implement this datatype
    Set<Email> emails;
    Map<Email, Boolean> readStatus;

    public MailBox() {
        emails = new HashSet<>();
        readStatus = new HashMap<>();
    }

    /**
     * Add a new message to the mailbox
     *
     * @param msg the message to add
     * @return true if the message was added to the mailbox,
     * and false if it was not added to the mailbox (because a duplicate exists
     * or msg was null)
     */
    public boolean addMsg(Email msg) {
        if (emails.contains(msg) || msg == null) {return false;}
        emails.add(msg);
        readStatus.put(msg, false);
        return true;
    }


    /**
     * Return the email with the provided id
     * @param msgID the id of the email to retrieve, is not null
     * @return the email with the provided id
     * and null if such an email does not exist in this mailbox
     */
    public Email getMsg(UUID msgID) {
        for (Email email : emails) {
            if (email.getId().equals(msgID)) {
                return email;
            }
        }
        return null;
    }

    /**
     * Delete a message from the mailbox
     *
     * @param msgId the id of the message to delete
     * @return true if the message existed in the mailbox and it was removed,
     * else return false
     */
    public boolean delMsg(UUID msgId) {
        for (Email email : emails) {
            if (email.getId().equals(msgId)) {
                emails.remove(email);
                readStatus.remove(email);
                return true;
            }
        }
        return false;
    }

    /**
     * Obtain the number of messages in the mailbox
     *
     * @return the number of messages in the mailbox
     */
    public int getMsgCount() {
        return emails.size();
    }

    /**
     * Mark the message with the given id as read
     *
     * @param msgID the id of the message to mark as read, is not null
     * @return true if the message exists in the mailbox and false otherwise
     */
    public boolean markRead(UUID msgID) {
        for (Email email : emails) {
            if (email.getId().equals(msgID)) {
                readStatus.replace(email, true);
                return true;
            }
        }
        return false;
    }

    /**
     * Mark the message with the given id as unread
     *
     * @param msgID the id of the message to mark as unread, is not null
     * @return true if the message exists in the mailbox and false otherwise
     */
    public boolean markUnread(UUID msgID) {
        for (Email email : emails) {
            if (email.getId().equals(msgID)) {
                readStatus.replace(email, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the specified message has been read or not
     *
     * @param msgID the id of the message to check, is not null
     * @return true if the message has been read and false otherwise
     * @throws IllegalArgumentException if the message does not exist in the mailbox
     */
    public boolean isRead(UUID msgID) {
        for (Email email : emails) {
            if (email.getId().equals(msgID)) {
                return readStatus.get(email);
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Obtain the number of unread messages in this mailbox
     * @return the number of unread messages in this mailbox
     */
    public int getUnreadMsgCount() {
        int count = 0;
        for (Email email : emails) {
            if (!readStatus.get(email)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Obtain a list of messages in the mailbox, sorted by timestamp,
     * with most recent message first
     *
     * @return a list that represents a view of the mailbox with messages sorted
     * by timestamp, with most recent message first. If multiple messages have
     * the same timestamp, the ordering among those messages is arbitrary.
     */
    public List<Email> getTimestampView() {
        Set<Email> emailsLeft = new HashSet<>(emails);
        List<Email> timestampView = new ArrayList<>();
        int min;
        Email nextEmail;
        while (!emailsLeft.isEmpty()) {
            min = Integer.MIN_VALUE;
            nextEmail = null;
            for (Email email : emailsLeft) {
                if (min < email.getTimestamp()) {
                    min = email.getTimestamp();
                    nextEmail = email;
                }
            }
            timestampView.add(nextEmail);
            emailsLeft.remove(nextEmail);
        }
        return timestampView;
    }

    /**
     * Obtain all the messages with timestamps such that
     * startTime <= timestamp <= endTime,
     * sorted with the earliest message first and breaking ties arbitrarily
     *
     * @param startTime the start of the time range, >= 0
     * @param endTime   the end of the time range, >= startTime
     * @return all the messages with timestamps such that
     * startTime <= timestamp <= endTime,
     * sorted with the earliest message first and breaking ties arbitrarily
     */
    public List<Email> getMsgsInRange(int startTime, int endTime) {
        Set<Email> emailsLeft = new HashSet<>(emails);
        List<Email> timestampView = new ArrayList<>();
        int min;
        Email nextEmail;
        while (!emailsLeft.isEmpty()) {
            min = Integer.MAX_VALUE;
            nextEmail = null;
            for (Email email : emailsLeft) {
                if (min > email.getTimestamp()) {
                    min = email.getTimestamp();
                    nextEmail = email;
                }
            }
            timestampView.add(nextEmail);
            emailsLeft.remove(nextEmail);
        }
        List<Email> removed = new ArrayList<>();
        for (Email email : timestampView) {
            if (email.getTimestamp() < startTime || email.getTimestamp() > endTime) {
                removed.add(email);
            }
        }
        timestampView.removeAll(removed);
        return timestampView;
    }


    /**
     * Mark all the messages in the same thread as the message
     * with the given id as read
     * @param msgID the id of a message whose entire thread is to be marked as read
     * @return true if a message with that id is in this mailbox
     * and false otherwise
     */
    public boolean markThreadAsRead(UUID msgID) {
        if (msgID == null || !emails.contains(getMsg(msgID))) {return false;}
        Set<Email> eThread = new HashSet<>();
        Set<Email> threadBase = new HashSet<>();
        Set<Set<Email>> allThreads = new HashSet<>();
        for (Email email : emails) {
            if (email.getResponseTo().equals(Email.NO_PARENT_ID)) {
                threadBase.add(email);
            }
        }
        for (Email email : threadBase) {
            Set<Email> currentThread = new HashSet<>();
            Email next = getMsg(email.getResponseTo());
            while (next != null) {
                currentThread.add(next);
                next = getMsg(next.getResponseTo());
            }
            allThreads.add(currentThread);
        }
        for (Set<Email> thread : allThreads) {
            if (thread.contains(getMsg(msgID))) {
                eThread = thread;
            }
        }
        for (Email email : eThread) {
            markRead(email.getId());
        }
        return true;
    }

    /**
     * Mark all the messages in the same thread as the message
     * with the given id as unread
     * @param msgID the id of a message whose entire thread is to be marked as unread
     * @return true if a message with that id is in this mailbox
     * and false otherwise
     */
    public boolean markThreadAsUnread(UUID msgID) {
        if (msgID == null || !emails.contains(getMsg(msgID))) {return false;}
        Set<Email> eThread = new HashSet<>();
        Set<Email> threadBase = new HashSet<>();
        Set<Set<Email>> allThreads = new HashSet<>();
        for (Email email : emails) {
            if (email.getResponseTo().equals(Email.NO_PARENT_ID)) {
                threadBase.add(email);
            }
        }
        for (Email email : threadBase) {
            Set<Email> currentThread = new HashSet<>();
            Email next = getMsg(email.getResponseTo());
            while (next != null) {
                currentThread.add(next);
                next = getMsg(next.getResponseTo());
            }
            allThreads.add(currentThread);
        }
        for (Set<Email> thread : allThreads) {
            if (thread.contains(getMsg(msgID))) {
                eThread = thread;
            }
        }
        for (Email email : eThread) {
            markUnread(email.getId());
        }
        return true;
    }

    /**
     * Obtain a list of messages, organized by message threads.
     * <p>
     * The message thread view organizes messages by starting with the thread
     * that has the most recent activity (based on timestamps of messages in the
     * thread) first, and within a thread more recent messages appear first.
     * If multiple emails within a thread have the same timestamp then the
     * ordering among those messages is arbitrary. Similarly, if more than one
     * thread can be considered "most recent", those threads can be ordered
     * arbitrarily.
     * <p>
     * A thread is identified by using information in an email that indicates
     * whether an email was in response to another email. The group of emails
     * that can be traced back to a common parent email message form a thread.
     *
     * @return a list that represents a thread-based view of the mailbox.
     */
    public List<Email> getThreadedView() {
        // TODO: Implement this method
        return null;
    }


}
