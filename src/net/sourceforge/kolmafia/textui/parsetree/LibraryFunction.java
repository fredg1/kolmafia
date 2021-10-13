package net.sourceforge.kolmafia.textui.parsetree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sourceforge.kolmafia.KoLmafia;
import net.sourceforge.kolmafia.RequestLogger;
import net.sourceforge.kolmafia.StaticEntity;
import net.sourceforge.kolmafia.textui.AshRuntime;
import net.sourceforge.kolmafia.textui.RuntimeLibrary;
import net.sourceforge.kolmafia.textui.ScriptException;
import net.sourceforge.kolmafia.textui.ScriptRuntime;

public class LibraryFunction extends Function {
  private Method method;

  public LibraryFunction(final String name, final Type type, final Type[] params) {
    super(name.toLowerCase(), type);

    Class[] args = new Class[params.length + 1];

    args[0] = ScriptRuntime.class;

    // Make a list of VariableReferences, even though the library
    // function will not use them, so that tracing works
    for (int i = 1; i <= params.length; ++i) {
      Variable variable = new Variable(params[i - 1]);
      this.variableReferences.add(new VariableReference(null, variable));
      args[i] = Value.class;
    }

    try {
      this.method = RuntimeLibrary.findMethod(name, args);
    } catch (Exception e) {
      // This should not happen; it denotes a coding
      // error that must be fixed before release.

      StaticEntity.printStackTrace(e, "No method found for built-in function: " + name);
    }
  }

  @Override
  public Value execute(final AshRuntime interpreter, Object[] values) {
    if (!KoLmafia.permitsContinue()) {
      interpreter.setState(ScriptRuntime.State.EXIT);
      return null;
    }

    if (StaticEntity.isDisabled(this.getName())) {
      this.printDisabledMessage(interpreter);
      return this.getType().initialValue();
    }

    if (this.method == null) {
      throw interpreter.runtimeException("Internal error: no method for " + this.getName());
    }

    try {
      // Bind values to variable references.
      // Collapse values into VarArgs array
      values = this.bindVariableReferences(interpreter, values);

      // Invoke the method
      return (Value) this.method.invoke(this, values);
    } catch (InvocationTargetException e) {
      // This is an error in the called method. Pass
      // it on up so that we'll print a stack trace.

      Throwable cause = e.getCause();
      if (cause instanceof ScriptException) {
        // Pass up exceptions intentionally generated by library
        throw (ScriptException) cause;
      }
      throw new RuntimeException(cause);
    } catch (IllegalAccessException e) {
      // This is not expected, but is an internal error in ASH
      throw new ScriptException(e);
    }
  }

  // This is necessary for calls into the runtime library from other languages.
  public Value executeWithoutInterpreter(ScriptRuntime controller, Object[] values) {
    if (StaticEntity.isDisabled(this.getName())) {
      RequestLogger.printLine("Called disabled function: " + this.getName());
      return this.getType().initialValue();
    }

    if (this.method == null) {
      throw controller.runtimeException("Internal error: no method for " + this.getName());
    }

    try {
      // Collapse values into VarArgs array
      values = this.bindVariableReferences(null, values);

      // Invoke the method
      return (Value) this.method.invoke(this, values);
    } catch (InvocationTargetException e) {
      // This is an error in the called method. Pass
      // it on up so that we'll print a stack trace.

      Throwable cause = e.getCause();
      if (cause instanceof ScriptException) {
        // Pass up exceptions intentionally generated by library
        throw (ScriptException) cause;
      }
      throw new RuntimeException(cause);
    } catch (IllegalAccessException e) {
      // This is not expected, but is an internal error in ASH
      throw new ScriptException(e);
    }
  }
}
