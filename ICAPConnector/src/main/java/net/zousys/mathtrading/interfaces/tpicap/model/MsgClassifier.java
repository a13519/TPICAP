package net.zousys.mathtrading.interfaces.tpicap.model;

import lombok.AllArgsConstructor;

import java.util.Set;

/**
 *
 */
@AllArgsConstructor
public class MsgClassifier {
    private Set<String> biztypes;
    private Set<String> exclusion;
    /**
     *
     * @param type
     * @return
     */
    public boolean isBizType(String type) {
        return biztypes.contains(type);
    }
    /**
     *
     * @param type
     * @return
     */
    public boolean isExcluded(String type) {
        return exclusion.contains(type);
    }

    /**
     *
     * @param type
     * @return
     */
    public boolean isQualified(String type) {
        return isBizType(type)&&!isExcluded(type);
    }
}
