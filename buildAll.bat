cd microprofile.jdt && .\mvnw.cmd clean install && cd .. ^
cd quarkus.jdt.ext && .\mvnw.cmd clean verify && cd .. ^
cd microprofile.ls\org.eclipse.lsp4mp.ls && .\mvnw.cmd clean install && cd ..\.. ^
cd quarkus.ls.ext\com.redhat.quarkus.ls && .\mvnw.cmd clean verify && cd ..\..