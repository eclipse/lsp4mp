# Contribution Guide

Contributions are extremely welcome, no matter how big or small. If you have any questions or suggestions we are happy to hear them.

## Raising issues or feature requests

Please raise any bug reports or feature requests on the [issue tracker](https://github.com/eclipse/lsp4mp/issues). Be sure to search the list to see if your issue has already been raised.

A good bug report is one that make it easy for us to understand what you were trying to do and what went wrong. Provide as much context as possible so we can try to recreate the issue.

## Project Structure and implementing language features

For documentation on project structure and how to implement new language features in lsp4mp you can look at the [contributing guide of vscode-microprofile](https://github.com/redhat-developer/vscode-microprofile/blob/master/CONTRIBUTING.md) which contains an overview of lsp4mp.

## File Headers

If you create a new Java file include the following header at the top of your file.

```
/****************** *************************************************************
* Copyright (c) {date} {owner}[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
*******************************************************************************/
```

## Eclipse Contributor Agreement

Before your contribution can be accepted by the project, you need to create and electronically sign an [Eclipse Contributor Agreement (ECA)](http://www.eclipse.org/legal/ecafaq.php):

1. Log in to the [Eclipse foundation website](https://accounts.eclipse.org/user/login/). You will need to
   create an account with the Eclipse Foundation if you have not already done so.
2. Click on "Eclipse ECA", and complete the form.

Be sure to use the same email address in your Eclipse account that you intend to use when you commit to GitHub.
All committers all commits are bound to the [Developer Certificate of Origin.](https://www.eclipse.org/legal/DCO.php)
As such, all parties involved in a contribution must have valid ECAs and commits must include [valid "Signed-off-by" entries.](https://wiki.eclipse.org/Development_Resources/Contributing_via_Git)
Commits can be signed off by including the `-s` attribute in your commit message, for example, `git commit -s -m 'Interesting Commit Message.`