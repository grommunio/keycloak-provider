grommunio Keycloak provider
===========================

**grommunio Keycloak provider enables Keycloak installations to integrate the grommunio
user backend with Keycloak.**

|shield-agpl|_ |shield-release|_ |shield-loc|

.. |shield-agpl| image:: https://img.shields.io/badge/license-AGPL--3.0-green
.. _shield-agpl: LICENSE
.. |shield-release| image:: https://shields.io/github/v/tag/grommunio/keycloak-provider
.. _shield-release: https://github.com/grommunio/keycloak-provider/tags
.. |shield-loc| image:: https://img.shields.io/github/languages/code-size/grommunio/keycloak-provider

At a glance
===========

* Provides user authentication support for any Keycloak installation
* Enables Keycloak-enabled applications to authenticate with grommunio
  backend, with support for grommunio's (Multi-)LDAP and non-LDAP users
* Provide OpenID Connect, OAuth 2.0 and SAML for grommunio and third-party
  applications
* Provide single sign-on, user federation, and role-based access control
  through Keycloak

Built with
==========

* Keycloak >= 20.0.1
* OpenJDK >=8, 17 recommended
* Maven

Getting started
===============

Prerequisites
-------------

* A working **Keycloak** installation, check out grommunio-auth for
  simplified deployment

Installation
------------

* Build with ``mvn package``
* Install to Keycloak provider location, defaults to
  `/opt/grommunio-keycloak/providers` with grommunio-keycloak provided
  installations

Support
=======

Support is available through grommunio GmbH and its partners. See
https://grommunio.com/ for details. A community forum is at
`<https://community.grommunio.com/>`_.

For direct contact and supplying information about a security-related
responsible disclosure, contact `dev@grommunio.com <dev@grommunio.com>`_.

Contributing
============

* https://docs.github.com/en/get-started/quickstart/contributing-to-projects
* Alternatively, upload commits to a git store of your choosing, or export the
  series as a patchset using `git format-patch
  <https://git-scm.com/docs/git-format-patch>`_, then convey the git
  link/patches through our direct contact address (above).
