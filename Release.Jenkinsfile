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
      string(name: 'VERSION', defaultValue: '', description: 'Version to Release?')
  }
  stages {
    stage("Release LSP4MP Language Server"){
      steps {
        script {
          if (!params.VERSION) {
            error('Not releasing')
          }
        }
        withMaven {
          sh '''
                cd microprofile.ls/org.eclipse.lsp4mp.ls
                VERSION=${params.VERSION}
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
            VERSION=${params.VERSION}
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
            msg="Release ${params.VERSION}"
            git commit -sm msg
            git tag ${params.VERSION}
            git push origin ${params.VERSION}
          '''
        }
      }
    }
  }
}
