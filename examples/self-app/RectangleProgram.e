let Rectangle = \w.\h. {
	width = w,
	height = h,

	area = \this. {
		this.width * this.height
	},

	toString = \this. {
		str(this.width) ++ "x" ++ str(this.height)
	},

	{}
};
let rect = Rectangle 10 20 in
let a = rect.area rect in
let s = rect.toString rect in
"area=" ++ str a ++ ", toString=" ++ s;;
