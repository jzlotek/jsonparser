'com.jzlotek.jsonparser'
========================

### Java based JSON field remover


##### To use

Given a JSON object like:

```json
{
	"field1":1,
    "field2":2,
    "field3":{
    			"childField1":"A",
                "childField2":"B",
                "childField3":"C"
             }
}
```

Create a list or a single String of a path i.e. if you only want to include ```"childField2"```, then the path is ```field3.childField2``` and the return String is:
```json
{
    "field3":{
                "childField2":"B"
             }
}
```


You can use a list also: ```["field3.childField2","field3.childField1"]``` which will return:
```json
{
    "field3":{
                "childField1":"A",
                "childField2":"B"
             }
}
```

Removing fields in arrays:
```json
{
    "field1":{[
                {
                	"childField1":"A",
             	   	"childField2":"B"
                },{
               	 	"childField1":"C",
               	 	"childField2":"D"
                }
                ]}
}
```
Calling with path: ```field1.childField1``` will result in:
```json
{
    "field1":{[
                {
                	"childField1":"A"
                },{
                	"childField1":"C"
                }
                ]}
}
```
It does not remove a specific entry in an array

Call JsonFieldRemovingUtil.{specific function}

Functions can be:
* ```getJsonString(Object object, {List<String> paths OR String path})``` - returns ```java.lang.String```
* ```getJsonNode(Object object, {List<String> paths OR String path})``` - returns ```org.codehaus.jackson.JsonNode```