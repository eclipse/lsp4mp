pipeline{
  agent any
  tools {
    jdk 'adoptopenjdk-hotspot-jdk8-latest'
  }
  environment {
    MAVEN_HOME = "$WORKSPACE/.m2/"
    MAVEN_USER_HOME = "$MAVEN_HOME"
  }
  stages{
    stage("Build LSP4MP JDT.LS extension"){
        steps {
          withMaven {
            sh 'cd microprofile.jdt && ./mvnw clean verify -B && cd ..'
          }
        }
    }
    stage("Build LSP4MP Language Server"){
        steps {
          withMaven {
            sh 'cd microprofile.ls/org.eclipse.lsp4mp.ls && ./mvnw clean verify -B'
          }
        }
    }
  }
}