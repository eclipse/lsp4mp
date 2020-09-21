pipeline {
  agent any
  tools {
    jdk 'adoptopenjdk-hotspot-jdk11-latest'
  }
  environment {
    MAVEN_HOME = "$WORKSPACE/.m2/"
    MAVEN_USER_HOME = "$MAVEN_HOME"
  }
  parameters {
      booleanParam(name: 'PERFORM_RELEASE', defaultValue: false, description: 'Perform a release?')
  }
  stages {
    stage("Release LSP4MP Language Server"){
      steps {
        script {
          if (!params.PERFORM_RELEASE) {
            error('Not releasing')
          }
        }
        withMaven {
          sh '''
                cd microprofile.ls/org.eclipse.lsp4mp.ls
                ./mvnw versions:set -DremoveSnapshot=true
                VERSION=$(./mvnw -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)

                ./mvnw versions:set-scm-tag -DnewTag=$VERSION
                ./mvnw clean deploy -B -Peclipse-sign -Dcbi.jarsigner.skip=false

                cd ../../microprofile.jdt
                ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION
                ./mvnw versions:set-scm-tag -DnewTag=$VERSION
                ./mvnw clean verify -B  -Peclipse-sign
                cd ..
              '''
        }
      }
    }

    stage('Deploy to downloads.eclipse.org') {
      steps {
        sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
          sh '''
            VERSION=`grep -o '[0-9].*[0-9]' microprofile.jdt/org.eclipse.lsp4mp.jdt.core/target/maven-archiver/pom.properties`
            targetDir=/home/data/httpd/download.eclipse.org/lsp4mp/releases/$VERSION
            ssh genie.lsp4mp@projects-storage.eclipse.org rm -rf $targetDir
            ssh genie.lsp4mp@projects-storage.eclipse.org mkdir -p $targetDir
            scp -r microprofile.jdt/org.eclipse.lsp4mp.jdt.site/target/*.zip genie.lsp4mp@projects-storage.eclipse.org:$targetDir
            ssh genie.lsp4mp@projects-storage.eclipse.org unzip $targetDir/*.zip -d $targetDir/repository
            '''
        }
      }
    }

    stage('Push tag to git') {
      steps {
        sshagent ( ['github-bot-ssh']) {
          sh '''
            git config --global user.email "lsp4mp-bot@eclipse.org"
            git config --global user.name "LSP4MP GitHub Bot"
            git add .
            msg="Release $VERSION"
            git commit -sm msg
            git tag $VERSION
            git push origin $VERSION
          '''
        }
      }
    }
  }
}
