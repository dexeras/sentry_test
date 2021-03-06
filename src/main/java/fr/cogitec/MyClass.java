package fr.cogitec;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.UserBuilder;
import io.sentry.event.interfaces.ExceptionInterface;

public class MyClass {
    private static SentryClient sentry;

    public static void main(String... args) {
        Sentry.init("https://4e64e1d179994f7d8de7f0849a14351d:43f50dc77bbb42e08007ca783dfa67bf@sentry.io/1195846");
        /*
        It is possible to go around the static ``Sentry`` API, which means
        you are responsible for making the SentryClient instance available
        to your code.
        */
        sentry = SentryClientFactory.sentryClient();

        MyClass myClass = new MyClass();
        //myClass.logWithStaticAPI();
        //myClass.logWithInstanceAPI();
        myClass.logSimpleMessage();
        //myClass.logException();
    }

    /**
     * An example method that throws an exception.
     */
    private void unsafeMethod() {
        throw new UnsupportedOperationException("You shouldn't call this!");
    }

    /**
     * Examples using the (recommended) static API.
     */
    private void logWithStaticAPI() {
        // Note that all fields set on the context are optional. Context data is copied onto
        // all future events in the current context (until the context is cleared).

        // Record a breadcrumb in the current context. By default the last 100 breadcrumbs are kept.
        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("User made an action").build()
        );

        // Set the user in the current context.
        Sentry.getContext().setUser(
                new UserBuilder().setEmail("hello@sentry.io").build()
        );

        // Add extra data to future events in this context.
        Sentry.getContext().addExtra("extra", "thing");

        // Add an additional tag to future events in this context.
        Sentry.getContext().addTag("tagName", "tagValue");

        /*
        This sends a simple event to Sentry using the statically stored instance
        that was created in the ``main`` method.
        */
        Sentry.capture("This is a test");

        try {
            unsafeMethod();
        } catch (Exception e) {
            // This sends an exception event to Sentry using the statically stored instance
            // that was created in the ``main`` method.
            Sentry.capture(e);
        }
    }

    /**
     * Examples that use the SentryClient instance directly.
     */
    private void logWithInstanceAPI() {
        // Retrieve the current context.
        Context context = sentry.getContext();

        // Record a breadcrumb in the current context. By default the last 100 breadcrumbs are kept.
        context.recordBreadcrumb(new BreadcrumbBuilder().setMessage("User made an action").build());

        // Set the user in the current context.
        context.setUser(new UserBuilder().setEmail("hello@sentry.io").build());

        // This sends a simple event to Sentry.
        sentry.sendMessage("This is a test");

        try {
            unsafeMethod();
        } catch (Exception e) {
            // This sends an exception event to Sentry.
            sentry.sendException(e);
        }
    }

    private void logSimpleMessage() {
        // This sends an event to Sentry.
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage("This is a test")
                .withLevel(Event.Level.INFO)
                .withLogger(MyClass.class.getName());

        // Note that the *unbuilt* EventBuilder instance is passed in so that
        // EventBuilderHelpers are run to add extra information to your event.
        Sentry.capture(eventBuilder);
    }

    private void logException() {
        try {
            unsafeMethod();
        } catch (Exception e) {
            // This sends an exception event to Sentry.
            EventBuilder eventBuilder = new EventBuilder()
                    .withMessage("Exception caught")
                    .withLevel(Event.Level.ERROR)
                    .withLogger(MyClass.class.getName())
                    .withSentryInterface(new ExceptionInterface(e));

            // Note that the *unbuilt* EventBuilder instance is passed in so that
            // EventBuilderHelpers are run to add extra information to your event.
            Sentry.capture(eventBuilder);
        }
    }
}