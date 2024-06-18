# Frontend

This frontend (website) is the part of the application that is visible to the user. It displays the ships and their information on the map, in addition to providing some other features (such as, notifications).

Frontend is implemented using [React.js framework](https://react.dev/).

## Table of Contents
- [Running Frontend](#running-frontend)
- [Running Tests and Static Checks](#running-tests-and-static-checks)
- [Icon Sources](#icon-sources)

## Running Frontend

**Before starting the frontend, you need to set up and start the Druid, Kafka and backend.** The instructions can be found in the [README.md in the folder above](../README.md).

When you are ready to start the frontend, you need to install the required packages and run the frontend using the following commands:
```shell
cd codebase/frontend

npm ci        # Install the Node packages based on 
              # the package.json and package-lock.json.
              
npm run start # The website can now be reached at
              # http://localhost:3000/.
```

If you want to run the production build, then you need to run the following command and follow the instructions written there:
```shell
npm run build
```

## Running tests and static checks

To ensure code quality of the frontend code, the following tools are used (also included in the GitLab pipeline):
- [Jest testing framework](https://jestjs.io/)
- [ESLint linter](https://eslint.org/)
- [Prettier formatter](https://prettier.io/)

Before running any of these, go to the `frontend` folder, and install the Node packages:
```shell
cd codebase/frontend
npm ci
```

Then you can run the tests and other checks using the following commands:
```shell
npm run lint            # Runs ESLint.
npm run prettier:check  # Runs Prettier to check the formatting.
npm run prettier:format # Runs Prettier to automatically 
                        # format the code.
npm run test:ci         # Runs the test and generates report in the folder 
                        # codebase/frontend/coverage/lcov-report.
```

## Icon Sources

- bell-notifications.svg - taken from https://iconoir.com/
- settings.svg - taken from https://iconoir.com/
- ship.png - taken from https://www.freepik.com
- back.svg - taken from https://flowbite.com/icons/
- close.png - taken from https://flowbite.com/icons/
