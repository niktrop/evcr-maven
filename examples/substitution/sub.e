let Variable = \name.
{
	name = name,

	applySubToSelf = \this.\s.  {
		let tail = s.tail;

		if (this.name == s.x.name) s.v
		else tail.apply tail this
	},

	toString = \this.  {
		this.name
	},

	{}
};

let IdentitySub =
{
	apply = \this.\item.  {
		item
	},

	applySubToSelf = \this.\s.  {
		s
	},

	toString = \this.  {
		"{}"
	},

	{}
};

let ExtendedSub = \x.\v.\tail.
{
	x = x,
	v = v,
	tail = tail,

	apply = \this.\item.  {
		item.applySubToSelf item this
	},

	applySubToSelf = \this.\s.  {
		let newV = s.apply s (this.v);
		let newTail = s.apply s (this.tail);

		{v=newV, tail=newTail, this}
	},

	toString = \this.  {
		let x = this.x;
		let v = this.v;
		let tail = this.tail;

		x.toString x ++ ":=" ++ v.toString v ++ "," ++ tail.toString tail
	},

	{}
};

let a = Variable "a";
let b = Variable "b";
let c = Variable "c";
let d = Variable "d";
let id = IdentitySub;
let s1 = ExtendedSub b a id;
let s2 = ExtendedSub a b (ExtendedSub c d id);
let s1s2 = s1.apply s1 s2;

let s1Str = s1.toString s1;
let s2Str = s2.toString s2;
let s1s2Str = s1s2.toString s1s2;

"s1 is " ++ s1Str ++ " , s2 is " ++ s2Str ++ " , s1s2 is " ++ s1s2Str;;
