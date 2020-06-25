package edu.uchicago.cs.db.subattr.datagen;

public class DataGen1 extends GenAddress {
    private GenHelper helper;

    int limit;
    int counter = 0;

    public void reset() {
        counter = 0;
    }

    public DataGen1(int limit) {
        this.limit = limit;
        this.helper = new GenHelper();
    }

    public String genPhone() {
        if (counter == limit)
            return null;
        counter++;
        StringBuilder builder = new StringBuilder();
        builder.append('(').append(helper.genDigit(3)).append(')')
                .append(helper.genDigit(3)).append('-').append(helper.genDigit(4));
        return builder.toString();
    }

    public String genIpv6() {
        if (counter == limit)
            return null;
        counter++;
        return String.join(":", helper.genHex(4), helper.genHex(4),
                helper.genHex(4), helper.genHex(4),
                helper.genHex(4), helper.genHex(4),
                helper.genHex(4), helper.genHex(4));
    }

    public String genTimestamp() {
        if (counter == limit)
            return null;
        counter++;
        StringBuilder builder = new StringBuilder();
        builder.append(String.join("-", helper.genRangeDigit(1970, 50), helper.genRangeDigit(1, 12), helper.genRangeDigit(1, 30))).append(' ');
        builder.append(String.join(":", helper.genRangeDigit(1, 24), helper.genRangeDigit(1, 60), helper.genRangeDigit(1, 60))).append(' ');
        builder.append(helper.genDigit(4)).append('.').append(helper.genDigit(5));
        return builder.toString();
    }

    public String genAddress() {
        if (counter == limit)
            return null;
        counter++;
        return generate();
    }

}
