package com.bric.core.domain.converter;

import com.bric.core.domain.enums.CorrespondenceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class EnumListConverter implements AttributeConverter<List<CorrespondenceType>, String> {

    @Override
    public String convertToDatabaseColumn(List<CorrespondenceType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream()
                .map(CorrespondenceType::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<CorrespondenceType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(CorrespondenceType::valueOf)
                .collect(Collectors.toList());
    }
}
