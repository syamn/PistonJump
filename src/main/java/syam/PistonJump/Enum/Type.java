/**
 * PistonJump - Package: syam.PistonJump.Enum
 * Created: 2012/09/17 11:38:31
 */
package syam.PistonJump.Enum;

import java.util.ArrayList;
import java.util.List;

/**
 * Type (Type.java)
 * @author syam(syamn)
 */
public enum Type {

	PLAYER("players"),	// プレイヤー
	BLOCK("blocks"),	// ブロック

	ITEM("items"),

	ANIMAL("animals"),	// 動物MOB
	MONSTER("monsters"),	// 敵対MOB

	MOB("mobs", ANIMAL, MONSTER),	// 全MOB

	ENTITY("entities", PLAYER, ITEM, MOB),	// エンティティ

	ALL("all", BLOCK, ENTITY),	// 全タイプ許容
	;


	private String plural;
	private List<Type> includes = new ArrayList<Type>();

	/**
	 * コンストラクタ
	 */
	Type(String plural, Type... includes){
		this.plural = plural;
	}

	/**
	 * 複数形名称を取得
	 * @return plural
	 */
	public String getPlural(){
		return this.plural;
	}


	/**
	 * 含むType型を返す
	 * @return includes
	 */
	public List<Type> getIncludes(){
		return this.includes;
	}
}
