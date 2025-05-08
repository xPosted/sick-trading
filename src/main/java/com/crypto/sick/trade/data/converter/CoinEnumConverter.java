package com.crypto.sick.trade.data.converter;

import com.crypto.sick.trade.dto.enums.CoinEnum;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter(autoApply = true)
public class CoinEnumConverter implements AttributeConverter<CoinEnum, String> {

    @Override
    public String convertToDatabaseColumn(CoinEnum attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public CoinEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CoinEnum.valueOf(dbData);
    }
}
