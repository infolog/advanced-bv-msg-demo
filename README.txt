goal:
reduce the number of messages which just contain e.g. a different label (e.g. for the validated property).
with jsf there is: javax.faces.validator.BeanValidator.MESSAGE={1}: {0}
that just covers a very simple use-case and the label can't be in the middle of the message.
furthermore, it's specific to jsf.

usage:

public class Person {
    @MySize(min = 1, max = 100, message = "{nameLength}", propertyName = "{firstName}")
    private String firstName;

    @MySize(min = 2, max = 100, message = "{nameLength}", propertyName = "{lastName}")
    private String lastName;
    
    //...
}

messages in the db:

nameLength=The length of '{propertyName}' should be between {min} and {max}
firstName=Firstname
lastName=Surname

example violation-message-text:
The length of 'Surname' should be between 2 and 100


the bv (composite-)constraint:

@ReportAsSingleViolation

@Size
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface MySize {
    String message();

    String propertyName();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @OverridesAttribute(constraint = Size.class, name = "min")
    int min() default 0;

    @OverridesAttribute(constraint = Size.class, name = "max")
    int max() default Integer.MAX_VALUE;

    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
    @interface List {
        MySize[] value();
    }
}
