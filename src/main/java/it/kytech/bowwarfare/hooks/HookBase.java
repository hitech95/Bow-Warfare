package it.kytech.bowwarfare.hooks;

public abstract class HookBase {
	
	public static class InvalidHookArgumentError extends Error {

		private static final long serialVersionUID = 1L;
		
		private String message;
		
		public InvalidHookArgumentError(String message) {
			this.message = message;
		}
		
		public InvalidHookArgumentError(Throwable t) {
			this.message = t.getMessage();
		}
		
		public String toString() {
			return "InvalidHookArgumentError@{" + message + "}";
		}
		
	}
	
	private boolean ready = false;
	
	public HookBase(String dependantClass) {
		if (dependantClass != null) {
			try {
				Class.forName(dependantClass);
			} catch (ClassNotFoundException | NoClassDefFoundError error) {
				return;
			}
		}
		ready = ready();
	}
	
	public final boolean isReady() {
		return (ready ? true : (ready = ready()));
	}
	
    public final boolean executeHook(String... args) {
    	try {
    		return execute(args);
    	} catch (InvalidHookArgumentError error) {}
    	return false;
    }
	
	protected abstract boolean ready();
	protected abstract boolean execute(String... args);

	public abstract String getShortName();
	
	public abstract Class<?>[] getParameters(); 

}
