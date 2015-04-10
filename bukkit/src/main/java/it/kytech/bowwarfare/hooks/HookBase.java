package it.kytech.bowwarfare.hooks;

public abstract class HookBase {

    private boolean ready = false;

    public HookBase(String dependantClass) {
        if (dependantClass != null) {
            try {
                Class.forName(dependantClass);
            } catch (NoClassDefFoundError e) {
                return;
            } catch (ClassNotFoundException e) {
                return;
            }
        }
        ready = ready();
    }

    protected abstract boolean ready();

    protected abstract boolean execute(String... args) throws InvalidHookArgumentError;

    public abstract String getShortName();

    public abstract Class<?>[] getParameters();

    public final boolean isReady() {
        return (ready ? true : (ready = ready()));
    }

    public final boolean executeHook(String... args) {
        try {
            return execute(args);
        } catch (InvalidHookArgumentError error) {
        }
        return false;
    }

    public static class InvalidHookArgumentError extends Error {

        private static final long serialVersionUID = 1L;

        private String message;

        public InvalidHookArgumentError(String message) {
            this.message = message;
        }

        public InvalidHookArgumentError(Throwable t) {
            this(t.getMessage());
        }

        @Override
        public String toString() {
            return "InvalidHookArgumentError@{ " + message + " }";
        }

        @Override
        public String getMessage() {
            return this.message;
        }

    }

}
