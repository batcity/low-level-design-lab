# Problem Statement and Requirements

Design a collaborative document editor that multiple users can view/edit concurrently

## Description

The idea here is to create something similar to google docs where multiple users can view or edit a document at the same time

## Functional Requirements

- Users should be able to create/retrieve/update/delete documents
- Concurrency: Multiple users must be able to view and edit the exact same document at the same time
- Changes made by one user should be propagated to all active collaborators in near real time
- User Identity: The system should track which user is making an edit (useful for conflict resolution during concurrent updates)

## Non functional Requirements

-  Non-blocking Operations: Document updates must be non-blocking to prevent locking out other active users.  
- Data Consistency: Concurrent edits should converge to the same final document state for all users. No user's accepted edit should be silently lost.
- Scope Isolation: Version 1 assumes all collaborators are connected to the same process/runtime. Network communication, distributed coordination, fault tolerance, and persistence across machines are out of scope.