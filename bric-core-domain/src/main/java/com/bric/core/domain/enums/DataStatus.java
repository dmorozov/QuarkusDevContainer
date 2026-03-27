package com.bric.core.domain.enums;

/**
 * Document lifecycle status. Ordinal values are significant —
 * used for "at or past" status comparison in duplicate detection.
 */
public enum DataStatus {
    PENDING,     // ordinal 0
    GENERATED,   // ordinal 1
    DISPATCHED,  // ordinal 2
    FAILED       // ordinal 3
}
