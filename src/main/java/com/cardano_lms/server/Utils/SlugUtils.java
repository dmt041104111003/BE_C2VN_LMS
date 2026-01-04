package com.cardano_lms.server.Utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");
    private static final String[][] VIETNAMESE_CHARS = {
        {"à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ"},
        {"a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a"},
        {"è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ"},
        {"e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "e"},
        {"ì", "í", "ị", "ỉ", "ĩ"},
        {"i", "i", "i", "i", "i"},
        {"ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ"},
        {"o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o"},
        {"ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ"},
        {"u", "u", "u", "u", "u", "u", "u", "u", "u", "u", "u"},
        {"ỳ", "ý", "ỵ", "ỷ", "ỹ"},
        {"y", "y", "y", "y", "y"},
        {"đ"},
        {"d"},
        {"À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ"},
        {"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"},
        {"È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ"},
        {"E", "E", "E", "E", "E", "E", "E", "E", "E", "E", "E"},
        {"Ì", "Í", "Ị", "Ỉ", "Ĩ"},
        {"I", "I", "I", "I", "I"},
        {"Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ"},
        {"O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O", "O"},
        {"Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ"},
        {"U", "U", "U", "U", "U", "U", "U", "U", "U", "U", "U"},
        {"Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ"},
        {"Y", "Y", "Y", "Y", "Y"},
        {"Đ"},
        {"D"}
    };
    
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        
        String result = input;
        result = removeVietnameseAccents(result);
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = WHITESPACE.matcher(result).replaceAll("-");
        result = NONLATIN.matcher(result).replaceAll("");
        result = MULTIPLE_DASHES.matcher(result).replaceAll("-");
        result = result.replaceAll("^-|-$", "");
        result = result.toLowerCase(Locale.ENGLISH);
        
        return result;
    }
    
    private static String removeVietnameseAccents(String str) {
        for (int i = 0; i < VIETNAMESE_CHARS.length; i += 2) {
            String[] accented = VIETNAMESE_CHARS[i];
            String[] unaccented = VIETNAMESE_CHARS[i + 1];
            for (int j = 0; j < accented.length; j++) {
                str = str.replace(accented[j], unaccented[j % unaccented.length]);
            }
        }
        return str;
    }
    
    public static String generateUniqueSlug(String title, java.util.function.Predicate<String> slugExists) {
        String baseSlug = toSlug(title);
        
        if (baseSlug.isEmpty()) {
            baseSlug = "course";
        }
        
        String slug = baseSlug;
        int counter = 1;
        
        while (slugExists.test(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
}

