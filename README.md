<div align="center">

# Open Interactive Simulation IDEA Plugin

[![Test](https://github.com/attiasas/ois-idea-plugin/actions/workflows/test.yml/badge.svg)](https://github.com/attiasas/ois-idea-plugin/actions/workflows/test.yml?branch=main)

</div>

## Table of Contents
- [📚 Overview](#-overview)
- [🐞 Troubleshooting](#-troubleshooting)

---
## 📚 Overview

---
## 🐞 Troubleshooting

You have the option of increasing the log level to DEBUG. Here's how to do it:

1. Go to: `Help` -> `Diagnostic Tools` -> `Debug Log Settings...`

2. Inside the `Custom Debug Log Configuration` window add the following line:

    ```text
    #org.ois.idea.log.Logger
    ```

To see the IDE log file, depending on the IDE version and OS as described [here](https://intellij-support.jetbrains.com/hc/en-us/articles/207241085-Locating-IDE-log-files),
go to: `Help` -> `Show/reveal Log in Explorer/finder/Konqueror/Nautilus`.