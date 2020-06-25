package edu.uchicago.cs.db.subattr.ir.matcher;

public class StringPiece implements CharSequence {

    private String source;

    int offset;

    int length;

    public StringPiece(String source, int startIndex, int endIndex) {
        this.source = source;
        this.offset = startIndex;
        this.length = endIndex - startIndex;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return source.charAt(offset + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return source.subSequence(offset + start, offset + end);
    }

    @Override
    public String toString() {
        return source.substring(offset, offset + length);
    }
}
