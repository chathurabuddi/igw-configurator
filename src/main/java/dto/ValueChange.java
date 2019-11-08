package dto;

/**
 * Copyright (c) 2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * <p>
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ValueChange {

    private String name;
    private String attribute;

    private String xpath;

    private String changeType;

    private String value;
    private String oldValue;
    private String add_node;
    private int pageno;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setCopy(String changeType) {
        this.changeType = changeType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getAdd_node() {
        return add_node;
    }

    public void setAdd_node(String add_node) {
        this.add_node = add_node;
    }

    public int getPageno() {
        return pageno;
    }

    public void setPageno(int pageno) {
        this.pageno = pageno;
    }
}