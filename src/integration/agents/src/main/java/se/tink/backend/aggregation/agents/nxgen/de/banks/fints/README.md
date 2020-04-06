# Introduction

## What is FinTs?
From wiki: FinTS (Financial Transaction Services), formerly known as HBCI (Home Banking Computer Interface), is a bank-independent protocol for online banking, developed and used by German banks.

In short, it is a way of getting information out of banks that was up and running before it was cool. Hipster PSD2. Banks expose a FinTS interface that we use to communicate with.

There are many versions of HBCI/FinTs. In our implementation we are focusing on `FinTS 3.0` as it is the version that banks support. Newer, xml based version 4.x is not yet supported by many banks.

## So what's the big whoop?

FinTs is weird. Messages used by the protocol look like this:

```
HNHBK:1:3+000000000148+300+0+1'
HKIDN:2:2+280:70150000+9999999999+0+0'
HKVVB:3:3+0+0+0+36792786FA12F235F04647689+3'
HKTAN:4:6+4+HKIDN++++N'
HNHBS:5:1+1'
```

Protocol is neatly explained in at least half a dozen of PDF files, that come only in German. They do talk a lot about syntax, message format, typical use cases and so on - on the surface it makes perfect sense, but the devil is in the details. Here are few examples:
 * Error messages provided by banks are usually very cryptic,
 * Some banks require TAN authorization earlier than others, for not really apparent reason,
 * Banks still use versions of segments that are no longer covered in the documentation,
 * How sessions are kept alive, or if at all, is not mentioned in the documentation,

In the end, even after getting familiar with the documentation there is still a lot of things to learn by just observing live organism of FinTs integration.


## Useful links

 * https://www.hbci-zka.de/ - main site about the protocol
 * https://www.hbci-zka.de/spec/3_0.htm - the docs that are of main interest to us


# Let's talk specifics

## Basics of FinTs

Here I want to explain the basics of communicating using FinTs protocol, to get you running quicker than going through the docs.

The communication structure is built upon small elements, all the way into something we will call session.
The building blocks are:

 * Session
 * Dialog
 * Message
 * Segment
 * Element group
 * Element
   
Between each of these, there is one-to-many relationship.

### Session
Not much I can say about it for now. During one session, usually more than one dialog between us and the bank will occur. What is important to note, bank never initializes a session.

There might be some way of keeping the session alive between uses, but that is to be confirmed. For now we start a new session on every separate interaction with the bank. (ie. on every manual refresh)

### Dialog
Dialog can contain many messages, sent by us, and response messages sent by bank. Bank will never initiate a dialog, it will only respond to the messages we send over.

There are some rules regarding when a new dialog has to be initiated, but it is hard to tell what exactly are they. For now, what we are doing is to have one dialog to initialize session, and then another dialog for everything else. To be confirmed if this is a good or best aproach.

### Message
The most interesting part of it all. The thing we work most often with, and what we think most often about. Just like any other protocol. Build a message, send it over, read what has been returned to you, decide what to do next.

Message is built by glueing together many segments. Segments need to be in a specific order, depending on what needs to be done. Examples of such messages will be covered later.

What is important to note, and can be seen in the example message above, is that segments are separated from each other inside a message body using `'` character.

### Segment, Element groups, Elements
Segment is a building block, in itself it doesn't hold much value, as many of them need to be stitched together into a message to make sense. It will look usually somewhat like this:

```HKTAN:4:6+4+HKIDN++++N'```

Every segment has a header. This header has a name, position of segment, and version of segment. Names are unique.

Segments are built from many elements. Elements can be grouped together into groups. Lets's split this example segment into groups. Groups are separated by `+` character:

 * `HKTAN:4:6`
 * `4`
 * `HKIDN`
 * 
 * 
 * 
 * `N`
 
So, this segment can be described as having 7 groups, first one is the header group, then we have two groups of one element each, three empty groups, and then again one group of one element. If you are interested about nitty gritty details of this segments, feel free to check [here](https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_2018-02-23_final_version.pdf) at page 45.

As you can probably see, the elements in a group are separated using `:`. Here is the first group split into single elements
 * `HKTAN`
 * `4`
 * `6`



## Grammar, syntax

### Special characters
 * `'`  -  used to separate segments in message
 * `+`  -  used to separate groups in segment
 * `:`  -  used to separate elements in group
 * `?`  -  escape character. Used when text element contains other special characters, eg. `asdf?:zxcv` should be understood as single element `asdf:zxcv` 
 * `@`  -  character used for binary elements, in a special fashion.

### Element types
Exact description of all element types that are accepted by FinTs protocol can be found [here](https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-10-06_final_version.pdf) page 21 & 22.

Most of them is pretty straightforward, but few require explanation:
 * boolean is represented `jn` type, `J` for true, and `N` for false
 * binary data is represented by `bin` type, eg. `@4@asdf`. Length of the binary content is prefixing the content, surrounded in `@`
 * floating point numbers of `float` type can end with a decimal point, eg. `1230,` is valid
 * date format is `yyyyMMdd`
 * time format is `hhmmss`


All types can be lumped into five groups then:
* text
  * in Java  -  `String`
  * in FinTs
    * `an`
	* `txt`
	* `bin`
	* `code`
	* `id`
	* `ctr` - country code
	* `cur` - currency code
* integer number
  * in Java  -  `Integer`
  * in FinTs
    * `num`
	* `dig`
* floating point number
  * in Java  -  `BigDecimal`
  * in FinTs
    * `float`
	* `wrt` - max length 15
* boolean
  * in Java  -  `Boolean`
  * in FinTs
    * `jn`
* date and time
  * in Java  -  `LocalDate/Time`
  * in FinTs
    * `dat`
	* `vdat`
	* `tim`


# Examples

Here are few examples of sanitized messages:

Session initialization request message:
```
HNHBK:1:3+000000000381+300+0+1'
HNVSK:998:3+PIN:1+998+1+1::0+1:20200313:125322+2:2:13:@8@00000000:5:1+280:75050000:asdfzxcv:S:0:0+0'
HNVSD:999:1+@220@HNSHK:2:4+PIN:1+999+1992499+1+1+1::0+1+1:20200313:125322+1:999:1+6:10:16+280:75050000:asdfzxcv:S:0:0'HKIDN:3:2+280:75050000+asdfzxcv+0+1'HKVVB:4:3+0+0+1+DCF3902F7888F5A831EA0F1D6+0.1'HKSYN:5:3+0'HNSHA:6:2+1992499++12345''
HNHBS:7:1+1'
```

Request message to close dialog:
```
HNHBK:1:3+000000000299+300+0+2'HNVSK:998:3+PIN:1+998+1+1::0+1:20200313:125323+2:2:13:@8@00000000:5:1+280:75050000:asdfzxcv:S:0:0+0'HNVSD:999:1+@138@HNSHK:2:4+PIN:1+999+2375649+1+1+1::0+1+1:20200313:125323+1:999:1+6:10:16+280:75050000:asdfzxcv:S:0:0'HKEND:3:1+0'HNSHA:4:2+2375649++12345''HNHBS:5:1+2'
```

Dialog closed response message:
```
HNHBK:1:3+000000000318+300+0+2+0:2'HNVSK:998:3+PIN:1+998+1+2::0+1+2:2:13:@8@00000000:5:1+280:75050000:asdfzxcv:S:0:0+0'HNVSD:999:1+@169@HNSHK:2:4+PIN:1+999+2375649+1+1+2::0+1+1+1:999:1+6:10:16+280:75050000:asdfzxcv:S:0:0'HIRMG:3:2+0010::Nachricht entgegengenommen.+0100::Dialog beendet.'HNSHA:4:2+2375649''HNHBS:5:1+2'
```

# Ok, but how do WE do it?

Syntax section touched on how we represent things in Java code. Let's continue! In this part we will cover main classes that act as building blocks of the whole communication, and try to explain why it was done this way.

We will cover these things:
 * FinTsRequest
   * Request segment classes
 * FinTsResponse
   * FinTsParser
   * Response segment classes

As you can see here and in code, the request class is totally separate from response class, as well as request segment classes don't really share anything with response segment classes. This was a concious decision. One could probably put an abstraction between them, and in some cases save a really small bit of code repetition, but it would mostly be artificial inheritance that bring more harm than good.

In case of request and response classes, each of them focuses on a problem that is an exact opoosite of each other. 

In case of segment classes, usually segments are not shared. There is usually a request segment, and a corresponding response segment, with a bit different name. Code duplication is thus minimal.

## FinTsRequest

Used when you want to construct a message to send over to the bank. 

It holds an ordered list of request segment objects.
It comes with a method to add a segment, and get a string representation of request.

By default, the created request has no segments. You can make use of static methods to get a encrypted envelope segment (used almost always in communication), to not have to create a boiler plate of segments.

By design, this class shouldn't know much about details of segments, or care at all about what segments have been added. Thers is one exception, unfortunately. Every proper request to FinTs comes with a header segment. This segments need to know the length of an entire message in string representation. So as a exception to a rule, FinTsRequest class knows that it has a header, and does some magic with it while building a string representation of itself.

### Request segment classes

Each and every segments we would like to send over in a request message to the bank needs to be represented by a class that extends `BaseRequestPart`. 

Classes are named after the segment name, followed by version suffix, eg.`HNSHAv2` represents a HNSHA segment in version 2.

Class should have fields, provide a builder for consistent creation experience, and implement `compile` method responsible for building a proper sequence of elements.

Here is an example:
```java
@Builder
public class HNSHAv2 extends BaseRequestPart {

    @NonNull private Integer securityReference;
    private String password;
    private String tanAnswer;

    @Override
    protected void compile() {
        super.compile();
        addGroup().element(securityReference);
        addGroup();
        addGroup().element(password).element(tanAnswer);
    }
}
```

It looks small, and it is! Please refer to the `BaseRequestPart` abstract class to see the details of methods used to create the groups and elements, as well as methods used by all of the request segments.

One last thing that might be worth explaining regarding request classes is the decision to have them versioned this way. The idea behind it is that we need to be very specific which version of segment we send. Some banks might not support certain versions of certain segments. We also don't want to have more builder parameters that are needed for that specific version of a segment, to limit the confusion to bare minimum while we try to use them.


## FinTsResponse

This class is instantiated when we receive a response from a bank, and want to extract information out of it. Parsing of the message string will be covered in the next subsection.
After message has been parsed, the raw representation of segments is stored in FinTsResponse object. Raw segment is pretty much just a list of lists of strings.

The class comes with few handy methods for finding a segment we want to work with. It takes a `Class` object as a parameter, neatly preventing us from looking for segments that have no implementation ready in one of the response segment classes, explained a bit more further below.

There are also few handy menthods that can report something about message as a whole. Eg. judge a message to be successful, or tell if the message has a specific status code. The logic of extracting anything that is contained in just one segment should be rather covered in response segment classes.

### FinTsParser

Designed to be dumb as a dodo. It acts in a linear fashion, going through a message left to right. Output of parsing procedure should be a list of list of list of strings. (List of segments, each represented by a list of groups, each represented by list of string elements) It goes like this:
 1. Open up a grand list for segments, create a empty segment, and an empty group
 2. look for an element
 3. add that element to the current group
 4. see what special character was at the end of the element
	* element separator? Do nothing
	* group separator? Add current group to segment, create new empty group
	* segment separator? Add current segment to result list, create new empty segment
 5. if not end of message, go to 2

And what if something isn't perfect? It will throw a tantrum and go home, leaving caller with a kindly worded exception


### Response segment classes

Separate from request segment classes for clarity. Each segment we want to extract some info out of needs a class to represent it.

But, unlike the request segments, one class houses all versions of the segment. Why is that? Because they don't really vary that much across versions, and we usually do not care about what specific version has been returned. We pretty much know that already, as the version of response segment is usually the same as request segment.

So, the response segment class should exted `BaseResponsePart` to gain some free functionality, and should have a contructor that takes `RawSegment` that will house all the logic of extracting any useful info out of list of list of strings into object fields, that in turn will be used via getters to do something awesome with.

Not every piece of information contained in RawSegment should be assigned to a field. We might only care about one or two field from the entire segment. Or we can prefer to lump some elements together to create more sensible representation for some data than it is done in FinTs format.

An example of response segment class looks like this:

```java
@Getter
public class HIRMS extends BaseResponsePart {

    @Getter
    @AllArgsConstructor
    public static class Response {
        private String resultCode;
        private String referenceElement;
        private String text;
        private List<String> parameters;
    }

    private List<Response> responses;

    public HIRMS(RawSegment rawSegment) {
        super(rawSegment);
        responses =
                rawSegment.getGroups().stream()
                        .skip(1)
                        .map(
                                group ->
                                        new Response(
                                                group.getString(0),
                                                group.getString(1),
                                                group.getString(2),
                                                group.subList(3, group.size())))
                        .collect(Collectors.toList());
    }

    public List<Response> getResponsesWithCode(String code) {
        return responses.stream()
                .filter(r -> code.equals(r.getResultCode()))
                .collect(Collectors.toList());
    }
}
```

