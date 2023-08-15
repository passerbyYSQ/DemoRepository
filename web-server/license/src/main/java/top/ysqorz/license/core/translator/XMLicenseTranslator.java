package top.ysqorz.license.core.translator;

import top.ysqorz.license.api.TrialLicense;
import top.ysqorz.license.api.TrialLicenseTranslator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/8/7
 */
public class XMLicenseTranslator implements TrialLicenseTranslator {
    @Override
    public TrialLicense translate(String plainText) {
        try {
            JAXBContext context = JAXBContext.newInstance(TrialLicense.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(plainText);
            return (TrialLicense) unmarshaller.unmarshal(reader);
        } catch (JAXBException ignored) {
            return null;
        }
    }

    @Override
    public String translate(TrialLicense trialLicense) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TrialLicense.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(trialLicense, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException ignored) {
            return null;
        }
    }
}
