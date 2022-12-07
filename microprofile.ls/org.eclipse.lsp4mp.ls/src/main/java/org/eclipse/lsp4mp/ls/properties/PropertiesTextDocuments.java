package org.eclipse.lsp4mp.ls.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.ls.commons.ModelTextDocument;
import org.eclipse.lsp4mp.ls.commons.ModelTextDocuments;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.utils.PropertiesFileUtils;
import org.eclipse.lsp4mp.utils.URIUtils;

public class PropertiesTextDocuments extends ModelTextDocuments<PropertiesModel> {

	private final Map<String, PropertiesModel> closedModels;

	public PropertiesTextDocuments(BiFunction<TextDocument, CancelChecker, PropertiesModel> parse) {
		super(parse);
		this.closedModels = new HashMap<>();
	}

	@Override
	public ModelTextDocument<PropertiesModel> onDidOpenTextDocument(DidOpenTextDocumentParams params) {
		String uri = params.getTextDocument().getUri();
		if (closedModels.containsKey(uri)) {
			synchronized (closedModels) {
				this.closedModels.remove(uri);
			}
		}
		return super.onDidOpenTextDocument(params);
	}

	@Override
	public PropertiesModel getModel(String uri) {
		PropertiesModel openedModel = super.getModel(uri);
		if (openedModel != null) {
			return openedModel;
		}
		// vscode opens the file by encoding the file URI and the 'C' of hard drive
		// lower case
		// 'c'.
		// for --> file:///C:/Users/a folder/application.properties
		// vscode didOpen --> file:///c%3A/Users/a%20folder/application.properties
		String encodedFileURI = URIUtils.encodeFileURI(uri).toUpperCase();
		// We loop for all properties files which are opened and we compare the encoded
		// file URI with upper case
		for (ModelTextDocument<PropertiesModel> textDocument : all()) {
			if (textDocument.getUri().toUpperCase().equals(encodedFileURI)) {
				return textDocument.getModel();
			}
		}
		PropertiesModel closedModel = closedModels.get(uri);
		if (closedModel != null) {
			return closedModel;
		}
		synchronized (closedModels) {
			closedModel = closedModels.get(uri);
			if (closedModel != null) {
				return closedModel;
			}
			closedModel = PropertiesFileUtils.loadProperties(uri);
			if (closedModel != null) {
				closedModels.put(uri, closedModel);
			}
		}
		return closedModel;

	}

}
