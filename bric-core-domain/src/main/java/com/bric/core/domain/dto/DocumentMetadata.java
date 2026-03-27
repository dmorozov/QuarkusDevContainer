package com.bric.core.domain.dto;

import com.bric.core.domain.enums.CorrespondenceType;
import com.bric.core.domain.enums.DocumentType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "documentMetadata")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentMetadata {

    @XmlElement
    private DocumentType type;

    @XmlElementWrapper(name = "correspondenceTypes")
    @XmlElement(name = "correspondenceType")
    private List<CorrespondenceType> correspondenceTypes;

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "entry")
    private List<DataEntry> dataEntries;

    public DocumentMetadata() {}

    public DocumentMetadata(DocumentType type, List<CorrespondenceType> correspondenceTypes, Map<String, String> data) {
        this.type = type;
        this.correspondenceTypes = correspondenceTypes;
        this.dataEntries = data.entrySet().stream()
                .map(e -> new DataEntry(e.getKey(), e.getValue()))
                .toList();
    }

    public DocumentType getType() { return type; }
    public List<CorrespondenceType> getCorrespondenceTypes() { return correspondenceTypes; }
    public Map<String, String> getData() {
        if (dataEntries == null) return Map.of();
        return dataEntries.stream().collect(
            Collectors.toMap(DataEntry::getKey, DataEntry::getValue));
    }

    public String toXml() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(DocumentMetadata.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(this, writer);
        return writer.toString();
    }

    public static DocumentMetadata fromXml(String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(DocumentMetadata.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (DocumentMetadata) unmarshaller.unmarshal(new StringReader(xml));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DataEntry {
        @XmlElement private String key;
        @XmlElement private String value;

        public DataEntry() {}
        public DataEntry(String key, String value) { this.key = key; this.value = value; }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }
}
