package com.epam.test;

/**
 * Created by Rauf_Aliev on 8/19/2016.
 */
public class TestBean {
    String stringProperty;
    Object refLink;

    public String getStringProperty() {
        testPrivateMethod( "getStringProperty" );
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
        testPublicMethod( "setStringProperty" );
    }

    public Object getRefLink() {
        testPrivateMethod( "getRefLink" );
        return refLink;

    }

    public void setRefLink(Object refLink) {
        this.refLink = refLink;
        testPublicMethod( "setRefLink" );
    }

    public void testPublicMethod(String a) {
        System.out.println("testPublicMethod "+a);
    }

    private void testPrivateMethod(String a) {
        System.out.println("testPrivateMethod "+a);
    }

}
