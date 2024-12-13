package design;

import java.util.HashSet;
import java.util.Stack;

// https://leetcode.com/problems/evaluate-reverse-polish-notation/description/

public class ReversePolishNotation {

    public int evalRPN(String[] tokens) {
        HashSet<String> operators = new HashSet<>();
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");

        Stack<Integer> stack = new Stack<>();

        for(String token : tokens) {
            if(operators.contains(token)) {
                int a = stack.pop();
                int b = stack.pop();
                switch(token) {
                    case "+" -> stack.push(b+a);
                    case "-" -> stack.push(b-a);
                    case "*" -> stack.push(b*a);
                    case "/" -> stack.push(b/a);
                }
            } else {
                stack.push(Integer.valueOf(token));
            }
        }
        return stack.pop();
    }
}
