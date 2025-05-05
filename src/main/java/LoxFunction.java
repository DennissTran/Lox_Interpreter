import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration; // The function declaration.

    LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration; // Initialize the function declaration.
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals); // Create a new environment for the function call.
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i)); // Define each parameter in the environment.
        }
        try {
            interpreter.executeBlock(declaration.body, environment); // Execute the function body in the new environment.
        } catch (Return returnValue) {
            return returnValue.value; // Return the value if a return statement is encountered.
        }
        return null; // If no return statement is encountered, return null.
    }

    @Override
    public int arity() {
        return declaration.params.size(); // Return the number of parameters.
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">"; 
    }
}
