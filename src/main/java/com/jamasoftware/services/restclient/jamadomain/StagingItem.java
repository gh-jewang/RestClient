package com.jamasoftware.services.restclient.jamadomain;

import com.jamasoftware.services.restclient.JamaParent;
import com.jamasoftware.services.restclient.exception.JamaFieldNotFound;
import com.jamasoftware.services.restclient.exception.JamaTypeMismatchException;
import com.jamasoftware.services.restclient.exception.RestClientException;
import com.jamasoftware.services.restclient.jamadomain.lazyresources.*;
import com.jamasoftware.services.restclient.jamadomain.stagingresources.StagingResource;
import com.jamasoftware.services.restclient.jamadomain.values.JamaFieldValue;
import com.jamasoftware.services.restclient.jamadomain.values.TextFieldValue;

import java.io.IOException;
import java.io.OutputStream;

public class StagingItem extends JamaItem implements StagingResource {
    // todo no fetch
    // only getters and setters for edits
    final private JamaItem originatingItem;

    protected StagingItem(){
        originatingItem = null;
    }

    protected StagingItem(JamaItem item) throws RestClientException{
        super(item);
        associate(item.getId(), item.getJamaInstance());
        originatingItem = item;
    }

    protected StagingItem(String name, JamaParent parent, JamaItemType itemType) throws RestClientException{
        setItemType(itemType);
        setName(name);
        setParent(parent);
        originatingItem = null;
    }

    @Override
    protected void copyContentFrom(JamaDomainObject jamaDomainObject) {
        super.copyContentFrom(jamaDomainObject);
    }

    public StagingItem setName(String name) {
        if(this.name == null) {
            this.name = (TextFieldValue) itemType.getField("name").getValue();
        }
        this.name.setValue(name);
        return this;
    }


    public StagingItem setGlobalId(String globalId) {
        //TODO need to change the query parameters
        this.globalId = globalId;
        return this;
    }

    public StagingItem setItemType(JamaItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public StagingItem setChildItemType(JamaItemType childItemType) {
        this.childItemType = childItemType;
        return this;
    }

    public StagingItem setModifiedBy(JamaUser modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public StagingItem addFieldValue(JamaFieldValue fieldValue) {
        fieldValues.add(fieldValue);
        return this;
    }

    public StagingItem setFieldValue(String fieldName, Object value) throws JamaTypeMismatchException, JamaFieldNotFound {
        if(fieldName.equals("name")) {
            setName(value.toString());
            return this;
        }
        JamaFieldValue replacementValue = itemType.getField(fieldName).getValue();
        replacementValue.setValue(value);
        replaceFieldValue(replacementValue);
        return this;
    }

    public StagingItem setFieldValueQuietly(String fieldName, Object value, OutputStream out) throws IOException{
        try{
            setFieldValue(fieldName, value);
        } catch(JamaTypeMismatchException | JamaFieldNotFound e) {
            //Deliberately ignoring exception
            if(out != null) {
                out.write(e.getMessage().getBytes());
            }
        }
        return this;
    }

    public StagingItem setFieldValueQuietly(String fieldName, Object value) {
        try{
            setFieldValueQuietly(fieldName, value, null);
        } catch(IOException e) {
            // No output stream passed in. No exception will be thrown
        }
        return this;
    }

    private void replaceFieldValue(JamaFieldValue value) throws JamaFieldNotFound {
        for(int i = 0; i < fieldValues.size(); ++i) {
            if(fieldValues.get(i).getName().equals(value.getName())) {
                fieldValues.set(i, value);
                return;
            }
        }
        throw new JamaFieldNotFound("Unable to locate field \"" + value.getName() + "\" in item " + this);
    }

    public void commit() throws RestClientException {
        if (getId() == null) {
            postCommit();
        } else {
            putCommit();
        }
    }

    private void postCommit() throws RestClientException {
        post();
    }

    private void putCommit() throws RestClientException {
        for(JamaFieldValue value : fieldValues) {
            System.out.println(value.getName() + ": " + value.getValue());
        }
        System.out.println("name: " + getName());
        put();
        invalidate(originatingItem);
    }

    @Override
    protected String getEditUrl() throws RestClientException {
        return "items/" + getId();
    }

    public StagingItem setParent(JamaParent item) throws RestClientException{
        this.makeChildOf(item);
        return this;
    }

    @Override
    public void associate(int id, JamaInstance jamaInstance) throws RestClientException {
        throw new RestClientException("You cannot associate an item while it is in edit mode.");
    }
}