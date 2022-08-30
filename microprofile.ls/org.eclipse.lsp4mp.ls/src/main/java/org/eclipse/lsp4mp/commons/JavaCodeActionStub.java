package org.eclipse.lsp4mp.commons;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;

/**
 * Represents a template of a code action that can be turned into an unresolved
 * code action provided a diagnostic.
 * 
 * @author datho7561
 */
public class JavaCodeActionStub {

	private String errorCode;
	private String id;
	private String messageTemplate;
	private String messageRegex; // optional

	public JavaCodeActionStub() {
	}

	public JavaCodeActionStub(String errorCode, String id, String messageTemplate, String messageRegex) {
		this.errorCode = errorCode;
		this.id = id;
		this.messageTemplate = messageTemplate;
		this.messageRegex = messageRegex;
	}

	/**
	 * Returns the code action for the given diagnostic, or null if this code action
	 * is not applicable for the diagnostic.
	 * 
	 * @param d the diagnostic that the code action should resolve
	 * @return the code action for the given diagnostic, or null if this code action
	 *         is not applicable for the diagnostic
	 */
	public CodeAction getUnresolvedCodeAction(Diagnostic d) {
		if (!isApplicableToDiagnostic(d)) {
			return null;
		}
		CodeAction ca = new CodeAction();
		ca.setDiagnostics(Arrays.asList(d));
		ca.setTitle(getFormattedMessage(d));
		ca.setKind(CodeActionKind.QuickFix);
		ca.setData(id); // TODO: make into an actual object?
		return ca;
	}

	/**
	 * Returns true if this code action is applicable to the given diagnostic, and
	 * false otherwise.
	 * 
	 * @param d the diagnostic to check if this code action can be applied to
	 * @return true if this code action is applicable to the given diagnostic, and
	 *         false otherwise
	 */
	private boolean isApplicableToDiagnostic(Diagnostic d) {
		return d.getCode().getLeft().equals(errorCode);
	}

	/**
	 * Returns the formatted message for this code action.
	 * 
	 * @param d the diagnostic that the code action will resolve
	 * @return the formatted message for this code action.
	 */
	private String getFormattedMessage(Diagnostic d) {
		if (messageRegex == null) {
			return messageTemplate;
		}

		Pattern p = Pattern.compile(messageRegex);
		Matcher m = p.matcher(d.getMessage());
		m.find();
		Object[] groups = new String[m.groupCount()];
		for (int i = 0; i < groups.length; i++) {
			groups[i] = m.group(i);
		}
		return MessageFormat.format(messageTemplate, groups);
	}

	/**
	 * Returns the unique identifier of the code action stub.
	 * 
	 * @return the unique identifier of the code action stub
	 */
	public String getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorCode, id, messageTemplate, messageRegex);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof JavaCodeActionStub)) {
			return false;
		}
		JavaCodeActionStub that = (JavaCodeActionStub) other;
		return Objects.equals(this.errorCode, that.errorCode) && Objects.equals(this.id, that.id)
				&& Objects.equals(this.messageTemplate, that.messageTemplate)
				&& Objects.equals(this.messageRegex, that.messageRegex);
	}

}
