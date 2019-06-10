# Depends on envsubst from gettext
_TEMPLATE_SH = """
set -euo pipefail
while read line; do
    export "${line% *}"="${line#* }"
done < "$VERSION_FILE" && \
while read line; do
    export "${line% *}"="${line#* }"
done < "$INFO_FILE" && \
cat "$TEMPLATE_FILE" | "$STABLE_ENVSUBST_PATH" > "$OUTFILE"
"""

def _templ_impl(ctx):
    env = {
        "INFO_FILE": ctx.info_file.path,
        "OUTFILE": ctx.outputs.outfile.path,
        "TEMPLATE_FILE": ctx.file.template.path,
        "VERSION_FILE": ctx.version_file.path,
    }

    # Add custom substitutions to the ENV
    env.update(ctx.attr.substitutions)

    ctx.actions.run_shell(
        outputs = [ctx.outputs.outfile],
        inputs = [
            ctx.version_file,  # Unstable status
            ctx.info_file,  # Stable status
            ctx.file.template,
        ],
        progress_message = "Generating service configuration: " + ctx.label.name,
        command = _TEMPLATE_SH,
        env = env,
    )

# templated_file takes a template file, and replaces the variables in bash format.
# Variables from "ctx.version_file" (--workspace_status_command) are availble by default
# Extra variables can be defined with the substitutions attribute.
templated_file = rule(
    implementation = _templ_impl,
    attrs = {
        "substitutions": attr.string_dict(),
        "template": attr.label(allow_single_file = True, mandatory = True),
    },
    outputs = {
        "outfile": "%{name}.out",
    },
)
