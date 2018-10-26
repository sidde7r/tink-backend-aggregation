# Contributing

## Git

### Commit messages
```
type(scope): message

details
```

- Example:
```
fix(LF/TransferData): Make getAccounts() null-safe

We got NPE when calling the getAccounts() method, so return new list if value is null.
```

The first line should be <= 72 chars long, since after 72 chars GitHub breaks the line and puts a `...` button on the side. For messages longer you add a `\n` and write more details on the next line after that.

Preferably first line should be <= 50 chars for readability purposes, but that’s not required at this time.

This purpose is to make it easier for e.g. a person in the `Platform` team to understand at a glimpse what has changed regarding `Integration` that might affect deployments and reasons to do rollbacks of recent deploys. Or for a person in the same team to understand what the others have been up to lately regarding code changes.

#### Types
- `build`: Changes that affect the build system or external dependencies (example scopes: broccoli, npm)
- `chore`: Other changes that don’t modify src or test files
- `ci`: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- `copy`: Copy update
- `docs`: Documentation only changes
- `feat`: A new feature
- `fix`: Bug fixes
- `log`: Add logging if we want to gather some insights
- `perf`: A code change that improves performance
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `revert`: Reverts a previous commit
- `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- `test`: Adding missing tests or correcting existing tests

#### Scope
A scope may be provided to a commit’s type, to provide additional contextual information and is contained within parenthesis, e.g., feat(parser): add ability to parse arrays. Scopes are heavily encouraged and should be used as much as possible.

E.g.

`AuthService`, `SEBAgent`, etc...

#### Message
Leave a space between the closing parenthesis and the message on the first line.

The message is supposed to give some more insights about ~~WHY~~ we’re doing the commit.

Don’t state implementation details that we know from just looking at the code, the more important part is understanding why we changed something for future references. E.g. if some credential triggered to handle something differently in a response model of a provider implementation.

#### Details
For messages that span longer than the first line limit: Leave an empty line, and after that put details about the commit on new lines as you’d like. This could include specifics on why we implemented something like we did, or certain parts that are important to note about the change.

The thought is still that a people going through the code half a year later should be able to figure out why something has changed into what it is today a little easier without full knowledge about the implementation.

## Code  Style
- Do not use static imports in production code. In tests feel free to do so.
- Prefer `equals` to compare objects instead of `==` to avoid confusion and possible bugs.
