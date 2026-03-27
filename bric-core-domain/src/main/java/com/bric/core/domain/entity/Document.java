package com.bric.core.domain.entity;

import com.bric.core.domain.converter.EnumListConverter;
import com.bric.core.domain.enums.CorrespondenceType;
import com.bric.core.domain.enums.DataStatus;
import com.bric.core.domain.enums.DocumentType;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "DOCUMENTS")
public class Document extends UpdatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_seq")
    @SequenceGenerator(name = "document_seq", sequenceName = "DOCUMENT_SEQ", allocationSize = 1)
    @Column(name = "DOCUMENT_ID")
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_STATUS", nullable = false)
    public DataStatus status = DataStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    public DocumentType type;

    @Convert(converter = EnumListConverter.class)
    @Column(name = "CORRESPONDENCE_LIST", nullable = false)
    public List<CorrespondenceType> correspondenceTypes;

    @Column(name = "STORAGE_ID", length = 36, nullable = true)
    public String storageId;

    @Column(name = "METADATA", length = 4096, nullable = false)
    public String metadata;
}
