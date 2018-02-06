package org.aion.vm.precompiled;

import org.aion.base.type.Address;
import org.aion.crypto.ECKey;
import org.aion.crypto.ECKeyFac;
import org.aion.crypto.ISignature;
import org.aion.fastvm.DummyRepository;
import org.aion.vm.ExecutionResult;
import org.aion.vm.types.DataWord;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static com.google.common.truth.Truth.assertThat;

public class TotalCurrencyContractTest {

    private Address contractAddress = Address.wrap("0000000000000000000000000000000000000000000000000000000000000100");

    // working case
    @Test
    public void testIncreaseTotalByAmount() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 22000L;
        final long expectedEnergyLeft = 1000L;

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(k.getAddress()));
        ByteBuffer bb = ByteBuffer.allocate(18);

        // setup the inputs
        bb.put((byte) 0x0)          // input
                .put((byte) 0x0)    // signum
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(18 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();
        ExecutionResult res = tcc.execute(combined, inputEnergy);

        assertThat(res.getCode()).isEqualTo(ExecutionResult.Code.SUCCESS);
        assertThat(res.getNrgLeft()).isEqualTo(expectedEnergyLeft);
    }

    // failure suite
    @Test
    public void testIncorrectInputLength() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 21000L;
        final long expectedEnergyLeft = 0L;

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(k.getAddress()));

        ByteBuffer bb = ByteBuffer.allocate(17);

        // setup the inputs
        // oops we forgot to encode the signum!
        bb.put((byte) 0x0)          // input
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(17 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();
        ExecutionResult res = tcc.execute(combined, 21000);
        assertThat(res.getCode()).isEqualTo(ExecutionResult.Code.INTERNAL_ERROR);
    }

    @Test
    public void testInsufficientEnergy() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 20000L;
        final long expectedEnergyLeft = 0L;

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(k.getAddress()));
        ByteBuffer bb = ByteBuffer.allocate(18);

        // setup the inputs
        bb.put((byte) 0x0)          // input
                .put((byte) 0x0)    // signum
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(18 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();
        ExecutionResult res = tcc.execute(combined, inputEnergy);

        assertThat(res.getCode()).isEqualTo(ExecutionResult.Code.OUT_OF_NRG);
        assertThat(res.getNrgLeft()).isEqualTo(expectedEnergyLeft);
    }

    @Test
    public void testIncorrectSignature() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 21000L;
        final long expectedEnergyLeft = 0L;

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(k.getAddress()));
        ByteBuffer bb = ByteBuffer.allocate(18);

        // setup the inputs
        bb.put((byte) 0x0)          // input
                .put((byte) 0x0)    // signum
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(18 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();

        // modify the signature in the 110th byte (arbitrarily)
        combined[110] = (byte) ~combined[110];

        ExecutionResult res = tcc.execute(combined, inputEnergy);

        assertThat(res.getCode()).isEqualTo(ExecutionResult.Code.INTERNAL_ERROR);
        assertThat(res.getNrgLeft()).isEqualTo(expectedEnergyLeft);
    }

    @Test
    public void testIncorrectPublicKey() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 21000L;
        final long expectedEnergyLeft = 0L;

        ECKey j = ECKeyFac.inst().create();

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();

        // notice that we set the address of j instead of k
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(j.getAddress()));
        ByteBuffer bb = ByteBuffer.allocate(18);

        // setup the inputs
        bb.put((byte) 0x0)          // input
                .put((byte) 0x0)    // signum
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(18 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();
        ExecutionResult res = tcc.execute(combined, inputEnergy);

        assertThat(res.getCode()).isEqualTo(ExecutionResult.Code.INTERNAL_ERROR);
        assertThat(res.getNrgLeft()).isEqualTo(expectedEnergyLeft);
    }

    @Test
    public void testIncreaseTotalAndQuery() {
        final BigInteger value = BigInteger.valueOf(100);
        final long inputEnergy = 22000L;
        final long expectedEnergyLeft = 1000L;

        ECKey k = ECKeyFac.inst().create();

        DummyRepository repo = new DummyRepository();
        TotalCurrencyContract tcc = new TotalCurrencyContract(
                repo, contractAddress, Address.wrap(k.getAddress()));
        ByteBuffer bb = ByteBuffer.allocate(18);

        // setup the inputs
        bb.put((byte) 0x0)          // input
                .put((byte) 0x0)    // signum
                .put(new DataWord(value.toByteArray()).getData());

        byte[] payload = bb.array();
        ISignature signature = k.sign(payload);

        bb = ByteBuffer.allocate(18 + 96);
        bb.put(payload);
        bb.put(signature.toBytes());
        byte[] combined = bb.array();
        ExecutionResult res = tcc.execute(combined, inputEnergy);

        // now lets query 0
        ExecutionResult queryResult = tcc.execute(new byte[]{(byte) 0x0}, inputEnergy);
        byte[] balance = queryResult.getOutput();

        assertThat(balance).isNotNull();
        assertThat(new BigInteger(balance)).isEqualTo(value);
    }
}