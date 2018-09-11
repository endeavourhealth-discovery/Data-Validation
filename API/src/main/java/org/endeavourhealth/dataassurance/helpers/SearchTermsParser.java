package org.endeavourhealth.dataassurance.helpers;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SearchTermsParser {
    private static final Logger LOG = LoggerFactory.getLogger(SearchTermsParser.class);

    private static List<String> cachedDateFormats = null;

    private String nhsNumber;
    private String emisNumber;
    private Date dateOfBirth;
    private List<String> names = new ArrayList<>();

    public SearchTermsParser(String searchTerms) {
        if (StringUtils.isEmpty(searchTerms))
            return;

        //String[] tokens = searchTerms.split(" ");
        List<String> tokens = Arrays.asList(searchTerms.split(" "));

        //remove any empty tokens before any further processing, so accidental double-spaces don't cause problems
        tokens = removeEmptyTokens(tokens);

        //not the nicest way of doing this, but if we have three separate numeric tokens that total 10 chars,
        //then mash them together as it's an NHS number search
        tokens = combineNhsNumberTokens(tokens);

        for (String token : tokens) {

            //try treating as a date
            dateOfBirth = tryParseDate(token);
            if (dateOfBirth != null) {
                continue;
            }

            //if contains any digits, try treating as an ID
            if (containsDigit(token)) {

                //if ten chars and purely digits, then treat as NHS number
                if (token.length() == 10
                        && StringUtils.isNumeric(token)) {
                    this.nhsNumber = token;

                } else {
                    this.emisNumber = token;
                }
                continue;
            }

            //otherwise treat as a name token
            this.names.add(token);
        }
    }

    private static boolean containsDigit(String token) {
        for (char c: token.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> getDateFormats() {
        if (cachedDateFormats == null) {
            List<String> l = new ArrayList<>();
            //l.add("ddMMyyyy"); removed as this will be confused with local IDs
            l.add("dd-MMM-yyyy");
            l.add("dd-MM-yyyy");
            l.add("dd/MMM/yyyy");
            l.add("dd/MM/yyyy");

            cachedDateFormats = l;
        }
        return cachedDateFormats;
    }

    private static Date tryParseDate(String token) {
        for (String format: getDateFormats()) {

            //compare length, as SimpleDateFormat doesn't validate it
            if (format.length() == token.length()) {
                SimpleDateFormat sf = new SimpleDateFormat(format);
                try {
                    return sf.parse(token);
                } catch (ParseException e2) {
                    //ignore the error
                }
            }
        }

        return null;
    }

    public boolean hasNhsNumber() {
        return StringUtils.isNotBlank(this.nhsNumber);
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public boolean hasEmisNumber() {
        return StringUtils.isNotBlank(this.emisNumber);
    }

    public String getEmisNumber() {
        return emisNumber;
    }

    public boolean hasDateOfBirth() {
        return this.dateOfBirth != null;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public List<String> getNames() {
        return names;
    }

    private static List<String> removeEmptyTokens(List<String> tokens) {
        List<String> result = new ArrayList<>();

        for(String token : tokens) {
            token = token.trim();
            if (!StringUtils.isEmpty(token)) {
                result.add(token);
            }
        }

        return result;
    }

    private static List<String> combineNhsNumberTokens(List<String> tokens) {
        if (tokens.size() != 3)
            return tokens;

        StringBuilder sb = new StringBuilder();

        for (String token : tokens) {
            if (StringUtils.isNumeric(token)) {
                sb.append(token);
            } else {
                return tokens;
            }
        }

        String combined = sb.toString();
        if (combined.length() != 10)
            return tokens;

        List<String> result = new ArrayList<>();
        result.add(combined);
        return result;
    }

    public boolean hasNames() {
        return names.size() > 0;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parsed Search Terms: ");

        if (!Strings.isNullOrEmpty(nhsNumber)) {
            sb.append("NHS Number: [");
            sb.append(nhsNumber);
            sb.append("] ");
        }

        if (!Strings.isNullOrEmpty(emisNumber)) {
            sb.append("Patient Number: [");
            sb.append(emisNumber);
            sb.append("] ");
        }

        if (dateOfBirth != null) {
            sb.append("Date of Birth: ");
            sb.append(dateOfBirth);
        }

        if (!names.isEmpty()) {
            sb.append("Names: ");
            for (String name: names) {
                sb.append(name);
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    public static String testParsing() {
        try {

            List<String> tests = new ArrayList<>();
            tests.add("24/05/1990"); //DoB
            tests.add("24-05-1990"); //DoB
            tests.add("24/MAY/1990"); //DoB
            tests.add("24-MAY-1990"); //DoB
            tests.add("1234567890"); //NHS number
            tests.add("24051978"); //patient ID
            tests.add("12123-132123-12312"); //patient ID
            tests.add("firstname lastname"); //names
            tests.add("firstname middlename lastname"); //names
            tests.add("firstname de-lastname"); //names with hyphen

            for (String test: tests) {
                SearchTermsParser parser = new SearchTermsParser(test);
                LOG.debug("from [" + test + "] -> " + parser);
            }

        } catch (Throwable t) {
            LOG.error("", t);
        }
    }
}
