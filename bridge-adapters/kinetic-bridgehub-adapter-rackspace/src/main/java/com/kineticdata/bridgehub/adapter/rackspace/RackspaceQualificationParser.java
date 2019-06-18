package com.kineticdata.bridgehub.adapter.rackspace;

import com.kineticdata.bridgehub.adapter.QualificationParser;

public class RackspaceQualificationParser extends QualificationParser {
    public String encodeParameter(String name, String value) {
        return value;
    }
}
