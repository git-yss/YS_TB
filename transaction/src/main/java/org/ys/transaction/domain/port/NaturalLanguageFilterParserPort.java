package org.ys.transaction.domain.port;

import java.util.Map;

public interface NaturalLanguageFilterParserPort {
    Map<String, Object> parseNaturalLanguage(String nlQuery);
}
