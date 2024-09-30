package com.example.practicerestapi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

@JsonComponent //objectMapper에 등록.. 언제? Erros객체 사용할때
public class ErrorsSerializer extends JsonSerializer<Errors> {
    /**
     * errors 를 json으로 나타내서 response하고 싶지만 그럴 수가 없다... 만들어보자
     */
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray(); //json 배열 시작
        errors.getFieldErrors().forEach(e -> {
            try {
                gen.writeStartObject();
                gen.writeStringField("field", e.getField());
                gen.writeStringField("objectName", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                Object rejectedValue = e.getRejectedValue();
                if(rejectedValue != null){
                    gen.writeStringField("rejectedValue", rejectedValue.toString());
                }
                gen.writeEndObject();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        errors.getGlobalErrors().forEach(e ->{
            try {
                gen.writeStartObject();
                gen.writeStringField("objectName", e.getObjectName());
                gen.writeStringField("code", e.getCode());
                gen.writeStringField("defaultMessage", e.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        gen.writeEndArray(); //json 배열 종료
    }
}
