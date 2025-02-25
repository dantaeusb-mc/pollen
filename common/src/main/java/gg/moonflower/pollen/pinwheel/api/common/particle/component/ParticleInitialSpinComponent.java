package gg.moonflower.pollen.pinwheel.api.common.particle.component;

import com.google.gson.JsonElement;
import gg.moonflower.pollen.api.util.JSONTupleParser;
import gg.moonflower.pollen.pinwheel.api.client.particle.CustomParticle;
import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;

/**
 * Component that specifies the initial rotation and rotation rate of a particle.
 *
 * @author Ocelot
 * @since 1.6.0
 */
public class ParticleInitialSpinComponent implements CustomParticleComponent, CustomParticleListener {

    private final MolangExpression rotation;
    private final MolangExpression rotationRate;

    public ParticleInitialSpinComponent(JsonElement json) {
        this.rotation = JSONTupleParser.getExpression(json.getAsJsonObject(), "rotation", () -> MolangExpression.ZERO);
        this.rotationRate = JSONTupleParser.getExpression(json.getAsJsonObject(), "rotation_rate", () -> MolangExpression.ZERO);
    }

    @Override
    public void onCreate(CustomParticle particle) {
        MolangEnvironment runtime = particle.getRuntime();
        particle.setRotation(this.rotation.safeResolve(runtime));
        particle.setRotationVelocity(this.rotationRate.safeResolve(runtime) / 20F);
    }
}
