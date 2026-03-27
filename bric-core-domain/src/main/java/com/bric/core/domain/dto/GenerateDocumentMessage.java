package com.bric.core.domain.dto;

public class GenerateDocumentMessage {

    private Long documentId;

    public GenerateDocumentMessage() {}

    public GenerateDocumentMessage(Long documentId) {
        this.documentId = documentId;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
}
